package com.chuckerteam.chucker.api

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.Chucker.LOG_TAG
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.api.internal.support.IOUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import okio.BufferedSource
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 *
 * @param context An Android [Context]
 * @param collector A [ChuckerCollector] to customize data retention
 * @param maxContentLength The maximum length for request and response content
 * before they are truncated. Warning: setting this value too high may cause unexpected
 * results.
 * @param headersToRedact List of headers that you want to redact. They will be not be shown in
 * the ChuckerUI but will be replaced with a `**`.
 */
class ChuckerInterceptor @JvmOverloads constructor(
        private val context: Context,
        private val collector: ChuckerCollector = ChuckerCollector(context),
        private val maxContentLength : Long = 250000L,
        private val headersToRedact : MutableList<String> = mutableListOf()
) : Interceptor {

    private val io: IOUtils = IOUtils(context)

    fun redactHeader(name: String) = apply {
        headersToRedact.add(name)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body()

        val transaction = HttpTransaction()
        transaction.requestDate = System.currentTimeMillis()
        transaction.method = request.method()
        transaction.populateUrl(request.url().toString())
        transaction.setRequestHeaders(request.headers())
        transaction.requestContentType = requestBody?.contentType()?.toString()
        transaction.requestContentLength = requestBody?.contentLength() ?: 0L

        val encodingIsSupported = io.bodyHasSupportedEncoding(request.headers().get("Content-Encoding"))
        transaction.isRequestBodyPlainText = encodingIsSupported

        if (requestBody != null && encodingIsSupported) {
            val source = io.getNativeSource(Buffer(), io.bodyIsGzipped(request.headers().get("Content-Encoding")))
            val buffer = source.buffer()
            requestBody.writeTo(buffer)
            var charset: Charset? = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
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
        } catch (e: Exception) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()

        // includes headers added later in the chain
        transaction.setRequestHeaders(filterHeaders(response.request().headers()))
        transaction.responseDate = System.currentTimeMillis()
        transaction.tookMs = tookMs
        transaction.protocol = response.protocol().toString()
        transaction.responseCode = response.code()
        transaction.responseMessage = response.message()

        transaction.responseContentType = responseBody?.contentType()?.toString()
        transaction.responseContentLength = responseBody?.contentLength() ?: 0L
        transaction.setResponseHeaders(filterHeaders(response.headers()))

        val responseEncodingIsSupported = io.bodyHasSupportedEncoding(response.headers().get("Content-Encoding"))
        transaction.isResponseBodyPlainText = responseEncodingIsSupported

        if (HttpHeaders.hasBody(response) && responseEncodingIsSupported) {
            val source = getNativeSource(response)
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer()
            var charset: Charset? = UTF8
            val contentType = responseBody?.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)
                } catch (e: UnsupportedCharsetException) {
                    collector.onResponseReceived(transaction)
                    return response
                }
            }
            if (io.isPlaintext(buffer)) {
                val content = io.readFromBuffer(buffer.clone(), charset, maxContentLength)
                transaction.responseBody = content
            } else {
                transaction.isResponseBodyPlainText = false
            }
            transaction.responseContentLength = buffer.size()
        }

        collector.onResponseReceived(transaction)

        return response
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
        if (io.bodyIsGzipped(response.headers().get("Content-Encoding"))) {
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
    }
}
