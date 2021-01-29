package com.chuckerteam.chucker.api

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import com.chuckerteam.chucker.internal.support.DepletingSource
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.ReportingSink
import com.chuckerteam.chucker.internal.support.RequestProcessor
import com.chuckerteam.chucker.internal.support.TeeSource
import com.chuckerteam.chucker.internal.support.contentType
import com.chuckerteam.chucker.internal.support.hasBody
import com.chuckerteam.chucker.internal.support.hasSupportedContentEncoding
import com.chuckerteam.chucker.internal.support.isProbablyPlainText
import com.chuckerteam.chucker.internal.support.uncompress
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.Source
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException
import kotlin.text.Charsets.UTF_8

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 */
public class ChuckerInterceptor private constructor(
    builder: Builder,
) : Interceptor {

    /**
     * An OkHttp Interceptor which persists and displays HTTP activity
     * in your application for later inspection.
     *
     * This constructor  is a shorthand for `ChuckerInterceptor.Builder(context).build()`.
     *
     * @param context An Android [Context]
     * @see ChuckerInterceptor.Builder
     */
    public constructor(context: Context) : this(Builder(context))

    private val context = builder.context
    private val collector = builder.collector ?: ChuckerCollector(context)
    private val maxContentLength = builder.maxContentLength
    private val cacheDirectoryProvider = builder.cacheDirectoryProvider ?: CacheDirectoryProvider { context.filesDir }
    private val alwaysReadResponseBody = builder.alwaysReadResponseBody
    private val headersToRedact = builder.headersToRedact.toMutableSet()
    private val requestProcessor = RequestProcessor(context, collector, maxContentLength)

    /** Adds [headerName] into [headersToRedact] */
    public fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val transaction = HttpTransaction()
        val request = requestProcessor.process(chain.request(), transaction)

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        processResponseMetadata(response, transaction)
        return multiCastResponseBody(response, transaction)
    }

    /**
     * Processes [Response] metadata and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponseMetadata(
        response: Response,
        transaction: HttpTransaction
    ) {
        val responseEncodingIsSupported = response.headers.hasSupportedContentEncoding

        transaction.apply {
            // includes headers added later in the chain
            setRequestHeaders(filterHeaders(response.request.headers))
            setResponseHeaders(filterHeaders(response.headers))

            isResponseBodyPlainText = responseEncodingIsSupported
            requestDate = response.sentRequestAtMillis
            responseDate = response.receivedResponseAtMillis
            protocol = response.protocol.toString()
            responseCode = response.code
            responseMessage = response.message

            response.handshake?.let { handshake ->
                responseTlsVersion = handshake.tlsVersion.javaName
                responseCipherSuite = handshake.cipherSuite.javaName
            }

            responseContentType = response.contentType

            tookMs = (response.receivedResponseAtMillis - response.sentRequestAtMillis)
        }
    }

    /**
     * Multi casts a [Response] body if it is available and downstreams it to a file which will
     * be available for Chucker to consume and save in the [transaction] at some point in the future
     * when the end user reads bytes form the [response].
     */
    private fun multiCastResponseBody(
        response: Response,
        transaction: HttpTransaction
    ): Response {
        val responseBody = response.body
        if (!response.hasBody() || responseBody == null) {
            collector.onResponseReceived(transaction)
            return response
        }

        val contentType = responseBody.contentType()
        val contentLength = responseBody.contentLength()

        val sideStream = ReportingSink(
            createTempTransactionFile(),
            ChuckerTransactionReportingSinkCallback(response, transaction),
            maxContentLength
        )
        var upstream: Source = TeeSource(responseBody.source(), sideStream)
        if (alwaysReadResponseBody) upstream = DepletingSource(upstream)

        return response.newBuilder()
            .body(upstream.buffer().asResponseBody(contentType, contentLength))
            .build()
    }

    private fun createTempTransactionFile(): File? {
        val cache = cacheDirectoryProvider.provide()
        return if (cache == null) {
            Logger.warn("Failed to obtain a valid cache directory for transaction files")
            null
        } else {
            FileFactory.create(cache)
        }
    }

    private fun processResponsePayload(
        response: Response,
        payload: Buffer,
        transaction: HttpTransaction
    ) {
        val responseBody = response.body ?: return

        val contentType = responseBody.contentType()
        val charset = contentType?.charset() ?: UTF_8

        if (payload.isProbablyPlainText) {
            transaction.isResponseBodyPlainText = true
            if (payload.size != 0L) {
                transaction.responseBody = payload.readString(charset)
            }
        } else {
            transaction.isResponseBodyPlainText = false

            val isImageContentType =
                (contentType?.toString()?.contains(CONTENT_TYPE_IMAGE, ignoreCase = true) == true)

            if (isImageContentType && (payload.size < MAX_BLOB_SIZE)) {
                transaction.responseImageData = payload.readByteArray()
            }
        }
    }

    /** Overrides all headers from [headersToRedact] with `**` */
    private fun filterHeaders(headers: Headers): Headers {
        val builder = headers.newBuilder()
        for (name in headers.names()) {
            if (headersToRedact.any { userHeader -> userHeader.equals(name, ignoreCase = true) }) {
                builder.set(name, "**")
            }
        }
        return builder.build()
    }

    private inner class ChuckerTransactionReportingSinkCallback(
        private val response: Response,
        private val transaction: HttpTransaction
    ) : ReportingSink.Callback {

        override fun onClosed(file: File?, sourceByteCount: Long) {
            file?.readResponsePayload()?.let { payload ->
                processResponsePayload(response, payload, transaction)
            }
            transaction.responsePayloadSize = sourceByteCount
            collector.onResponseReceived(transaction)
            file?.delete()
        }

        override fun onFailure(file: File?, exception: IOException) {
            Logger.error("Failed to read response payload", exception)
        }

        private fun File.readResponsePayload() = try {
            source().uncompress(response.headers).use { source ->
                Buffer().apply { writeAll(source) }
            }
        } catch (e: IOException) {
            Logger.error("Response payload couldn't be processed", e)
            null
        }
    }

    /**
     * Assembles a new [ChuckerInterceptor].
     *
     * @param context An Android [Context].
     */
    public class Builder(internal var context: Context) {
        internal var collector: ChuckerCollector? = null
        internal var maxContentLength = MAX_CONTENT_LENGTH
        internal var cacheDirectoryProvider: CacheDirectoryProvider? = null
        internal var alwaysReadResponseBody = false
        internal var headersToRedact = emptySet<String>()

        /**
         * Sets the [ChuckerCollector] to customize data retention.
         */
        public fun collector(collector: ChuckerCollector): Builder = apply {
            this.collector = collector
        }

        /**
         * Sets the maximum length for requests and responses content before their truncation.
         *
         * Warning: setting this value too high may cause unexpected results.
         */
        public fun maxContentLength(length: Long): Builder = apply {
            this.maxContentLength = length
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(headerNames: Iterable<String>): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(vararg headerNames: String): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * If set to `true` [ChuckerInterceptor] will read full content of response
         * bodies even in case of parsing errors or closing the response body without reading it.
         *
         * Warning: enabling this feature may potentially cause different behaviour from the
         * production application.
         */
        public fun alwaysReadResponseBody(enable: Boolean): Builder = apply {
            this.alwaysReadResponseBody = enable
        }

        /**
         * Sets provider of a directory where Chucker will save temporary responses
         * before processing them.
         */
        @VisibleForTesting
        internal fun cacheDirectorProvider(provider: CacheDirectoryProvider): Builder = apply {
            this.cacheDirectoryProvider = provider
        }

        /**
         * Creates a new [ChuckerInterceptor] instance with values defined in this builder.
         */
        public fun build(): ChuckerInterceptor = ChuckerInterceptor(this)
    }

    private companion object {
        private const val MAX_CONTENT_LENGTH = 250_000L
        private const val MAX_BLOB_SIZE = 1_000_000L

        private const val CONTENT_TYPE_IMAGE = "image"
    }
}
