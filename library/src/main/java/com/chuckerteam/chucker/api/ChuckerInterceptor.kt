package com.chuckerteam.chucker.api

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import com.chuckerteam.chucker.internal.support.DepletingSource
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.internal.support.IOUtils
import com.chuckerteam.chucker.internal.support.ReportingSink
import com.chuckerteam.chucker.internal.support.TeeSource
import com.chuckerteam.chucker.internal.support.contentType
import com.chuckerteam.chucker.internal.support.hasBody
import com.chuckerteam.chucker.internal.support.isGzipped
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import okio.Source
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import kotlin.jvm.Throws

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
    private val io = IOUtils(builder.context)
    private val headersToRedact = builder.headersToRedact.toMutableSet()

    /** Adds [headerName] into [headersToRedact] */
    public fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response
        val transaction = HttpTransaction()

        processRequest(request, transaction)
        collector.onRequestSent(transaction)

        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        processResponseMetadata(response, transaction)
        return multiCastResponseBody(response, transaction)
    }

    /**
     * Processes a [Request] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processRequest(request: Request, transaction: HttpTransaction) {
        val requestBody = request.body

        val encodingIsSupported = io.bodyHasSupportedEncoding(request.headers[CONTENT_ENCODING])

        transaction.apply {
            setRequestHeaders(request.headers)
            populateUrl(request.url)

            isRequestBodyPlainText = encodingIsSupported
            requestDate = System.currentTimeMillis()
            method = request.method
            requestContentType = requestBody?.contentType()?.toString()
            requestPayloadSize = requestBody?.contentLength() ?: 0L
        }

        if (requestBody != null && encodingIsSupported) {
            val source = io.getNativeSource(Buffer(), request.isGzipped)
            val buffer = source.buffer()
            requestBody.writeTo(buffer)
            var charset: Charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8) ?: UTF8
            }
            if (io.isPlaintext(buffer)) {
                val content = io.readFromBuffer(buffer, charset, maxContentLength)
                transaction.requestBody = content
            } else {
                transaction.isResponseBodyPlainText = false
            }
        }
    }

    /**
     * Processes [Response] metadata and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponseMetadata(
        response: Response,
        transaction: HttpTransaction
    ) {
        val responseEncodingIsSupported = io.bodyHasSupportedEncoding(response.headers[CONTENT_ENCODING])

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
            .body(ResponseBody.create(contentType, contentLength, upstream.buffer()))
            .build()
    }

    private fun createTempTransactionFile(): File? {
        val cache = cacheDirectoryProvider.provide()
        return if (cache == null) {
            IOException("Failed to obtain a valid cache directory for Chucker transaction file").printStackTrace()
            null
        } else {
            FileFactory.create(cache)
        }
    }

    private fun processResponseBody(
        response: Response,
        responseBodyBuffer: Buffer,
        transaction: HttpTransaction
    ) {
        val responseBody = response.body ?: return

        val contentType = responseBody.contentType()
        val charset = contentType?.charset(UTF8) ?: UTF8

        if (io.isPlaintext(responseBodyBuffer)) {
            transaction.isResponseBodyPlainText = true
            if (responseBodyBuffer.size != 0L) {
                transaction.responseBody = responseBodyBuffer.readString(charset)
            }
        } else {
            transaction.isResponseBodyPlainText = false

            val isImageContentType =
                (contentType?.toString()?.contains(CONTENT_TYPE_IMAGE, ignoreCase = true) == true)

            if (isImageContentType && (responseBodyBuffer.size < MAX_BLOB_SIZE)) {
                transaction.responseImageData = responseBodyBuffer.readByteArray()
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
            if (file != null) {
                val buffer = readResponseBuffer(file, response.isGzipped)
                if (buffer != null) {
                    processResponseBody(response, buffer, transaction)
                }
            }
            transaction.responsePayloadSize = sourceByteCount
            collector.onResponseReceived(transaction)
            file?.delete()
        }

        override fun onFailure(file: File?, exception: IOException) = exception.printStackTrace()

        private fun readResponseBuffer(responseBody: File, isGzipped: Boolean) = try {
            val bufferedSource = responseBody.source().buffer()
            val source = if (isGzipped) {
                GzipSource(bufferedSource)
            } else {
                bufferedSource
            }
            Buffer().apply { source.use { writeAll(it) } }
        } catch (e: IOException) {
            IOException("Response payload couldn't be processed by Chucker", e).printStackTrace()
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
        private val UTF8 = Charset.forName("UTF-8")

        private const val MAX_CONTENT_LENGTH = 250_000L
        private const val MAX_BLOB_SIZE = 1_000_000L

        private const val CONTENT_TYPE_IMAGE = "image"
        private const val CONTENT_ENCODING = "Content-Encoding"
    }
}
