package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.AndroidCacheFileFactory
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.internal.support.IOUtils
import com.chuckerteam.chucker.internal.support.TeeSource
import com.chuckerteam.chucker.internal.support.contentLength
import com.chuckerteam.chucker.internal.support.contentType
import com.chuckerteam.chucker.internal.support.hasBody
import com.chuckerteam.chucker.internal.support.isGzipped
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import okio.Okio

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 *
 * @param context An Android [Context]
 * @param collector A [ChuckerCollector] to customize data retention
 * @param maxContentLength The maximum length for request and response content
 * before their truncation. Warning: setting this value too high may cause unexpected
 * results.
 * @param fileFactory Provider for [File]s where Chucker will save temporary responses before
 * processing them.
 * @param headersToRedact a [Set] of headers you want to redact. They will be replaced
 * with a `**` in the Chucker UI.
 */
class ChuckerInterceptor internal constructor(
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
    private val maxContentLength: Long = 250000L,
    private val fileFactory: FileFactory,
    headersToRedact: Set<String> = emptySet()
) : Interceptor {

    /**
     * An OkHttp Interceptor which persists and displays HTTP activity
     * in your application for later inspection.
     *
     * @param context An Android [Context]
     * @param collector A [ChuckerCollector] to customize data retention
     * @param maxContentLength The maximum length for request and response content
     * before their truncation. Warning: setting this value too high may cause unexpected
     * results.
     * @param headersToRedact a [Set] of headers you want to redact. They will be replaced
     * with a `**` in the Chucker UI.
     */
    @JvmOverloads
    constructor(
        context: Context,
        collector: ChuckerCollector = ChuckerCollector(context),
        maxContentLength: Long = 250000L,
        headersToRedact: Set<String> = emptySet()
    ) : this(context, collector, maxContentLength, AndroidCacheFileFactory(context), headersToRedact)

    private val io: IOUtils = IOUtils(context)
    private val headersToRedact: MutableSet<String> = headersToRedact.toMutableSet()

    /** Adds [headerName] into [headersToRedact] */
    fun redactHeader(vararg headerName: String) {
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
        val requestBody = request.body()

        val encodingIsSupported = io.bodyHasSupportedEncoding(request.headers().get(CONTENT_ENCODING))

        transaction.apply {
            setRequestHeaders(request.headers())
            populateUrl(request.url())

            isRequestBodyPlainText = encodingIsSupported
            requestDate = System.currentTimeMillis()
            method = request.method()
            requestContentType = requestBody?.contentType()?.toString()
            requestContentLength = requestBody?.contentLength() ?: 0L
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
        val responseEncodingIsSupported = io.bodyHasSupportedEncoding(response.headers().get(CONTENT_ENCODING))

        transaction.apply {
            // includes headers added later in the chain
            setRequestHeaders(filterHeaders(response.request().headers()))
            setResponseHeaders(filterHeaders(response.headers()))

            isResponseBodyPlainText = responseEncodingIsSupported
            requestDate = response.sentRequestAtMillis()
            responseDate = response.receivedResponseAtMillis()
            protocol = response.protocol().toString()
            responseCode = response.code()
            responseMessage = response.message()

            response.handshake()?.let { handshake ->
                responseTlsVersion = handshake.tlsVersion().javaName()
                responseCipherSuite = handshake.cipherSuite().javaName()
            }

            responseContentType = response.contentType
            responseContentLength = response.contentLength

            tookMs = (response.receivedResponseAtMillis() - response.sentRequestAtMillis())
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
        val responseBody = response.body()
        if (!response.hasBody() || responseBody == null) {
            collector.onResponseReceived(transaction)
            return response
        }

        val contentType = responseBody.contentType()
        val contentLength = responseBody.contentLength()

        val teeSource = TeeSource(
            responseBody.source(),
            fileFactory.create(),
            ChuckerTransactionTeeCallback(response, transaction),
            maxContentLength
        )

        return response.newBuilder()
            .body(ResponseBody.create(contentType, contentLength, Okio.buffer(teeSource)))
            .build()
    }

    private fun processResponseBody(
        response: Response,
        responseBodyBuffer: Buffer,
        transaction: HttpTransaction
    ) {
        val responseBody = response.body() ?: return

        val contentType = responseBody.contentType()
        val charset = contentType?.charset(UTF8) ?: UTF8

        if (io.isPlaintext(responseBodyBuffer)) {
            transaction.isResponseBodyPlainText = true
            if (responseBodyBuffer.size() != 0L) {
                transaction.responseBody = responseBodyBuffer.readString(charset)
            }
        } else {
            transaction.isResponseBodyPlainText = false

            val isImageContentType =
                (contentType?.toString()?.contains(CONTENT_TYPE_IMAGE, ignoreCase = true) == true)

            if (isImageContentType && (responseBodyBuffer.size() < MAX_BLOB_SIZE)) {
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

    private inner class ChuckerTransactionTeeCallback(
        private val response: Response,
        private val transaction: HttpTransaction
    ) : TeeSource.Callback {
        override fun onSuccess(file: File) {
            val buffer = readResponseBuffer(file, response.isGzipped)
            file.delete()
            if (buffer != null) processResponseBody(response, buffer, transaction)
            collector.onResponseReceived(transaction)
        }

        override fun onFailure(exception: IOException, file: File) {
            file.delete()
            collector.onResponseReceived(transaction)
        }

        private fun readResponseBuffer(responseBody: File, isGzipped: Boolean): Buffer? {
            val bufferedSource = Okio.buffer(Okio.source(responseBody))
            val source = if (isGzipped) {
                GzipSource(bufferedSource)
            } else {
                bufferedSource
            }
            return try {
                Buffer().apply { writeAll(source) }
            } catch (_: IOException) {
                null
            }
        }
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        private const val MAX_BLOB_SIZE = 1000_000L

        private const val CONTENT_TYPE_IMAGE = "image"
        private const val CONTENT_ENCODING = "Content-Encoding"
    }
}
