package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.IOUtils
import com.chuckerteam.chucker.internal.support.contentLength
import com.chuckerteam.chucker.internal.support.contentType
import com.chuckerteam.chucker.internal.support.isGzipped
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
 * @param normalAPIDuration normal expected duration of api calls in millisecond, exceeding which it
 * will be marked as slow api call.
 */
class ChuckerInterceptor @JvmOverloads constructor(
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
    private val maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet(),
    private val normalAPIDuration: Long = -1L
) : Interceptor {

    private val io: IOUtils = IOUtils(context)
    private val headersToRedact: MutableSet<String> = headersToRedact.toMutableSet()

    /** Adds [headerName] into [headersToRedact] */
    fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .removeHeader(Chucker.SKIP_INTERCEPTOR_HEADER_NAME)
            .build()

        if (chain.request().header(Chucker.SKIP_INTERCEPTOR_HEADER_NAME)?.toBoolean() == true) {
            return chain.proceed(request)
        }

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

        val processedResponse = processResponse(response, transaction)
        collector.onResponseReceived(transaction)

        return processedResponse
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
     * Processes a [Response] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponse(response: Response, transaction: HttpTransaction): Response {
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

            tookMs?.let { tookMs ->
                if (normalAPIDuration > -1L && tookMs > normalAPIDuration) {
                    isSlowApiCall = true
                }
            }
        }

        return if (responseEncodingIsSupported) {
            processResponseBody(response, transaction)
        } else {
            response
        }
    }

    /**
     * Processes a [ResponseBody] and populates corresponding fields of a [HttpTransaction].
     */
    private fun processResponseBody(response: Response, transaction: HttpTransaction): Response {
        val responseBody = response.body() ?: return response

        val contentType = responseBody.contentType()
        val charset = contentType?.charset(UTF8) ?: UTF8
        val contentLength = responseBody.contentLength()

        val responseSource = if (response.isGzipped) {
            GzipSource(responseBody.source())
        } else {
            responseBody.source()
        }
        val buffer = Buffer().apply { responseSource.use { writeAll(it) } }

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
                transaction.responseImageData = buffer.clone().readByteArray()
            }
        }

        return response.newBuilder()
            .body(ResponseBody.create(contentType, contentLength, buffer))
            .build()
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
