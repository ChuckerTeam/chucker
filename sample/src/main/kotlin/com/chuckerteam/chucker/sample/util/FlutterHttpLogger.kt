package com.chuckerteam.chucker.sample.util

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource

/**
 * Data class representing HTTP log payload from Flutter
 */
data class HttpLogPayload(
    val url: String,
    val method: String,
    val requestHeaders: Map<String, Any?>? = null,
    val responseHeaders: Map<String, Any?>? = null,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val statusCode: Int? = null,
    val error: String? = null,
    val requestTime: Long? = null,
    val responseTime: Long? = null,
    val headerContentType: String? = null,
    val contentType: String? = null,
)

/**
 * Custom ResponseBody that can be read multiple times
 */
class ReusableResponseBody(
    private val content: String,
    private val mediaType: MediaType?,
) : ResponseBody() {
    private val contentBytes = content.toByteArray(Charsets.UTF_8)

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = contentBytes.size.toLong()

    override fun source(): BufferedSource {
        val buffer = Buffer()
        buffer.write(contentBytes)
        return buffer
    }
}

/**
 * Improved Flutter HTTP Logger that properly logs HTTP calls to Chucker
 */
class FlutterHttpLogger(
    private val context: Context,
) {
    private val chuckerInterceptor =
        ChuckerInterceptor
            .Builder(context)
            .alwaysReadResponseBody(true)
            .build()

    fun forwardHttpLogToHost(payload: HttpLogPayload) {
        try {
            Log.d("FlutterHttpLog", "Processing HTTP log: ${payload.method} ${payload.url}")

            // Create request headers
            val requestHeaders =
                Headers
                    .Builder()
                    .apply {
                        payload.requestHeaders?.forEach { (key, value) ->
                            add(key, value?.toString() ?: "")
                        }
                    }.build()

            // Create request body
            val requestMediaType =
                payload.headerContentType?.toMediaTypeOrNull()
                    ?: "application/json; charset=utf-8".toMediaType()

            val requestBody =
                payload.requestBody?.let { body ->
                    Log.d("FlutterHttpLog", "Request body: $body")
                    body.toRequestBody(requestMediaType)
                }

            // Create request
            val requestBuilder =
                Request
                    .Builder()
                    .url(payload.url)
                    .headers(requestHeaders)

            when {
                requestBody != null -> requestBuilder.method(payload.method, requestBody)
                payload.method.equals("GET", true) || payload.method.equals("HEAD", true) -> {
                    requestBuilder.method(payload.method, null)
                }

                else -> {
                    requestBuilder.method(payload.method, "".toRequestBody(requestMediaType))
                }
            }

            val request = requestBuilder.build()

            // Create response headers
            val responseHeaders =
                Headers
                    .Builder()
                    .apply {
                        payload.responseHeaders?.forEach { (key, value) ->
                            add(key, value?.toString() ?: "")
                        }
                    }.build()

            // Create response body with custom reusable implementation
            val responseMediaType =
                payload.contentType?.toMediaTypeOrNull()
                    ?: "application/json; charset=utf-8".toMediaType()

            val responseBodyContent = payload.responseBody ?: ""
            Log.d("FlutterHttpLog", "Response body: $responseBodyContent")
            Log.d("FlutterHttpLog", "Response media type: $responseMediaType")

            // Use custom ResponseBody that can be read multiple times
            val responseBody = ReusableResponseBody(responseBodyContent, responseMediaType)

            // Create response
            val actualRequestTime = payload.requestTime ?: System.currentTimeMillis()
            val actualResponseTime = payload.responseTime ?: System.currentTimeMillis()

            val response =
                Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(payload.statusCode ?: 200)
                    .message(payload.error ?: "OK")
                    .sentRequestAtMillis(actualRequestTime)
                    .receivedResponseAtMillis(actualResponseTime)
                    .headers(responseHeaders)
                    .body(responseBody)
                    .build()

            // Use DummyChain to process through ChuckerInterceptor
            val dummyChain = DummyChain(request, response, null)
            val processedResponse = chuckerInterceptor.intercept(dummyChain)

            // Consume the response body to trigger Chucker's data capture
            processedResponse.body.use { body ->
                body.string()
            }

            Log.d("FlutterHttpLog", "Successfully logged HTTP transaction to Chucker")
        } catch (e: Exception) {
            Log.e("FlutterHttpLog", "Error while processing HTTP log for Chucker: ${e.message}", e)
        }
    }
}
