package com.chuckerteam.chucker.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.sample.data.HttpLogPayload
import com.chuckerteam.chucker.sample.util.DummyChain
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit // Ensure this import is present

class MyFlutterNetworkLogger(context: Context) {

    // ChuckerInterceptor instance
    private val chuckerInterceptor = ChuckerInterceptor.Builder(context).build()

    fun forwardHttpLogToChucker(payload: HttpLogPayload) {
        // 1. Create OkHttp Request from payload
        val requestBody = payload.requestBody?.toRequestBody(payload.headerContentType?.toMediaTypeOrNull())

        val requestHeadersBuilder = Headers.Builder()
        payload.requestHeaders?.forEach { (key, value) ->
            if (value != null) {
                requestHeadersBuilder.add(key, value.toString())
            }
        }
        val requestHeaders = requestHeadersBuilder.build()

        val request = Request.Builder()
            .url(payload.url)
            .headers(requestHeaders)
            .method(payload.method, requestBody)
            .build()

        // 2. Create an OkHttp Response from payload
        val responseBody = payload.responseBody?.toResponseBody(payload.contentType?.toMediaTypeOrNull())

        val responseHeadersBuilder = Headers.Builder()
        payload.responseHeaders?.forEach { (key, value) ->
            if (value != null) {
                responseHeadersBuilder.add(key, value.toString())
            }
        }
        val responseHeaders = responseHeadersBuilder.build()

        val response = Response.Builder()
            .request(request) // Associate the response with the request
            .protocol(Protocol.HTTP_1_1) // You might try to infer this if payload has it
            .code(payload.statusCode?.toInt() ?: 200) // Default to 200 if null
            .message(payload.error ?: "OK") // Use error message if available, else "OK"
            .headers(responseHeaders)
            .body(responseBody)
            .sentRequestAtMillis(payload.requestTime ?: System.currentTimeMillis())
            .receivedResponseAtMillis(payload.responseTime ?: System.currentTimeMillis())
            .build()

        try {
            // 3. Process the request/response through the ChuckerInterceptor using a DummyChain
            val dummyChain = DummyChain(request, response)
            chuckerInterceptor.intercept(dummyChain)
            println("Logged HTTP transaction to Chucker: ${payload.url}") // Simple log for confirmation
        } catch (e: Exception) {
            System.err.println("Error while logging HTTP call to Chucker: ${e.message}")
            e.printStackTrace()
        }
    }
}
