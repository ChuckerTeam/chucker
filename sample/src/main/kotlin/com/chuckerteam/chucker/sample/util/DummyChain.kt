package com.fampay.`in`.helpers

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Connection
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Route
import okio.Buffer
import okio.Timeout
import java.io.IOException
import java.net.Socket
import java.util.concurrent.TimeUnit

class DummyChain(
    private val request: Request,
    private val response: Response?,
    private val exception: IOException?
) : Interceptor.Chain {

    override fun request(): Request = request

    override fun proceed(request: Request): Response {
        response?.body?.let { body ->
            val content = body.string()
            val newResponseBody = content.toByteArray().toResponseBody(body.contentType())

            return response.newBuilder()
                .body(newResponseBody)
                .build()
        }
        return response ?: throw IllegalStateException("Response cannot be null for proceed().")
    }


    override fun connection(): Connection = object : Connection {
        override fun socket(): Socket = Socket()
        override fun route(): Route = TODO("Route not required for dummy chain")
        override fun handshake(): Handshake? = null
        override fun protocol(): Protocol = Protocol.HTTP_1_1
    }

    override fun call(): Call = object : Call {
        override fun request(): Request = this@DummyChain.request
        override fun execute(): Response = response ?: throw IllegalStateException("No response")
        override fun enqueue(responseCallback: Callback) {}
        override fun cancel() {}
        override fun isExecuted(): Boolean = false
        override fun isCanceled(): Boolean = false
        override fun clone(): Call = this
        override fun timeout(): Timeout = Timeout.NONE
    }

    override fun connectTimeoutMillis(): Int = 10_000
    override fun readTimeoutMillis(): Int = 10_000
    override fun writeTimeoutMillis(): Int = 10_000

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
}
