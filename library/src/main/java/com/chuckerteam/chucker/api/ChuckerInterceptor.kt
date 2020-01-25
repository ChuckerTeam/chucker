package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.IOUtils
import com.chuckerteam.chucker.internal.support.hasBody
import java.io.IOException
import java.nio.charset.Charset
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource

private const val MAX_BLOB_SIZE = 1000_000L

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
class ChuckerInterceptor @JvmOverloads constructor(
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
    private val maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet()
) : Interceptor {

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

        processResponse(response, transaction)
        collector.onResponseReceived(transaction)

        return response
    }

    /**
     * Processes a [Request] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processRequest(request: Request, transaction: HttpTransaction) {
        val requestBody = request.body()

        val encodingIsSupported = io.bodyHasSupportedEncoding(request.headers().get(CONTENT_ENCODING))

        transaction.apply {
            setRequestHeaders(request.headers())
            populateUrl(request.url().toString())

            isRequestBodyPlainText = encodingIsSupported
            requestDate = System.currentTimeMillis()
            method = request.method()
            requestContentType = requestBody?.contentType()?.toString()
            requestContentLength = requestBody?.contentLength() ?: 0L
        }

        if (requestBody != null && encodingIsSupported) {
            val source = io.getNativeSource(Buffer(), io.bodyIsGzipped(request.headers().get(CONTENT_ENCODING)))
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
     * Processes a [Response] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponse(response: Response, transaction: HttpTransaction) {
        val responseBody = response.body()!!
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

            responseContentType = responseBody.contentType()?.toString()
            responseContentLength = responseBody.contentLength()

            tookMs = (response.receivedResponseAtMillis() - response.sentRequestAtMillis())
        }

        if (response.hasBody() && responseEncodingIsSupported) {
            processResponseBody(response, responseBody, transaction)
        }
    }

    /**
     * Processes a [ResponseBody] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponseBody(response: Response, responseBody: ResponseBody, transaction: HttpTransaction) {
        val contentType = responseBody.contentType()
        val charset: Charset = contentType?.charset(UTF8) ?: UTF8
        val contentLength = responseBody.contentLength()

        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        var buffer = source.buffer()

        if (io.bodyIsGzipped(response.headers()[CONTENT_ENCODING])) {
            GzipSource(buffer.clone()).use { gzippedResponseBody ->
                buffer = Buffer()
                buffer.writeAll(gzippedResponseBody)
            }
        }

        if (io.isPlaintext(buffer)) {
            transaction.isResponseBodyPlainText = true
            if (contentLength != 0L) {
                transaction.responseBody = buffer.clone().readString(charset)
            }
        } else {
            transaction.isResponseBodyPlainText = false

            val isImageContentType =
                (contentType?.toString()?.contains(CONTENT_TYPE_IMAGE, ignoreCase = true) == true)

            if (isImageContentType && buffer.size() < MAX_BLOB_SIZE) {
                transaction.responseImageData = buffer.readByteArray()
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

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        private const val MAX_BLOB_SIZE = 1000_000L

        private const val CONTENT_TYPE_IMAGE = "image"
        private const val CONTENT_ENCODING = "Content-Encoding"
    }
}
