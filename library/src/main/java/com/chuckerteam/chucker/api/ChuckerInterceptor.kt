package com.chuckerteam.chucker.api

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.Chucker.LOG_TAG
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.IOUtils
import com.chuckerteam.chucker.internal.support.hasBody
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource

private const val MAX_BLOB_SIZE = 1000_000L

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 *
 * @param context An Android [Context]
 * @param collector A [ChuckerCollector] to customize data retention
 * @param maxContentLength The maximum length for request and response content
 * before they are truncated. Warning: setting this value too high may cause unexpected
 * results.
 * @param headersToRedact Set of headers that you want to redact. They will be replaced
 * with a `**` in the ChuckerUI.
 */
class ChuckerInterceptor @JvmOverloads constructor(
    private val context: Context,
    private val collector: ChuckerCollector = ChuckerCollector(context),
    private val maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet()
) : Interceptor {

    private val io: IOUtils = IOUtils(context)
    private val headersToRedact: MutableSet<String> = headersToRedact.toMutableSet()

    fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    @Suppress("LongMethod")
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body()

        val transaction = HttpTransaction()
        transaction.apply {
            requestDate = System.currentTimeMillis()
            method = request.method()
            populateUrl(request.url().toString())
            setRequestHeaders(request.headers())
            requestContentType = requestBody?.contentType()?.toString()
            requestContentLength = requestBody?.contentLength() ?: 0L
        }

        val encodingIsSupported = io.bodyHasSupportedEncoding(request.headers().get(CONTENT_ENCODING))
        transaction.isRequestBodyPlainText = encodingIsSupported

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

        collector.onRequestSent(transaction)

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()

        // includes headers added later in the chain
        transaction.setRequestHeaders(filterHeaders(response.request().headers()))
        transaction.apply {
            responseDate = System.currentTimeMillis()
            this.tookMs = tookMs
            protocol = response.protocol().toString()
            responseCode = response.code()
            responseMessage = response.message()

            responseContentType = responseBody?.contentType()?.toString()
            responseContentLength = responseBody?.contentLength() ?: 0L
            setResponseHeaders(filterHeaders(response.headers()))
        }

        val responseEncodingIsSupported = io.bodyHasSupportedEncoding(response.headers().get(CONTENT_ENCODING))
        transaction.isResponseBodyPlainText = responseEncodingIsSupported

        if (response.hasBody() && responseEncodingIsSupported) {
            processResponseBody(response, responseBody, transaction)
        }

        collector.onResponseReceived(transaction)

        return response
    }

    /**
     * Private method to process the HTTP Response body and populate the corresponding response fields
     * of a the [HttpTransaction].
     */
    private fun processResponseBody(response: Response, responseBody: ResponseBody?, transaction: HttpTransaction) {
        getNativeSource(response).use { source ->
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer()
            var charset: Charset = UTF8
            val contentType = responseBody?.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8) ?: UTF8
                } catch (e: UnsupportedCharsetException) {
                    return
                }
            }
            if (io.isPlaintext(buffer)) {
                val content = io.readFromBuffer(buffer.clone(), charset, maxContentLength)
                transaction.responseBody = content
            } else {
                transaction.isResponseBodyPlainText = false

                if (transaction.responseContentType?.contains("image") == true && buffer.size() < MAX_BLOB_SIZE) {
                    transaction.responseImageData = buffer.clone().readByteArray()
                }
            }
            transaction.responseContentLength = buffer.size()
        }
    }

    /** Overrides all the headers in [headersToRedact] with a `**` */
    private fun filterHeaders(headers: Headers): Headers {
        val builder = headers.newBuilder()
        for (name in headers.names()) {
            if (name in headersToRedact) {
                builder.set(name, "**")
            }
        }
        return builder.build()
    }

    /**
     * Returns the [BufferedSource] of the response and also UnGzip it if necessary.
     */
    @Throws(IOException::class)
    private fun getNativeSource(response: Response): BufferedSource {
        if (io.bodyIsGzipped(response.headers().get(CONTENT_ENCODING))) {
            val source = response.peekBody(maxContentLength).source()
            if (source.buffer().size() < maxContentLength) {
                return io.getNativeSource(source, true)
            } else {
                Log.w(LOG_TAG, "gzip encoded response was too long")
            }
        }
        return response.body()!!.source()
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        private val CONTENT_ENCODING = "Content-Encoding"
    }
}
