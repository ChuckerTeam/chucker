package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.JsonConverter
import com.google.gson.reflect.TypeToken
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * A gRPC [ClientInterceptor] that records requests and responses for inspection in Chucker.
 *
 * Add it to your gRPC channel:
 * ```kotlin
 * val channel = OkHttpChannelBuilder.forAddress(host, port)
 *     .intercept(ChuckerGrpcInterceptor(collector, context))
 *     .build()
 * ```
 *
 * In release builds swap to [com.chuckerteam.chucker.api.ChuckerGrpcInterceptor] from the
 * `library-no-op` artifact to eliminate all overhead.
 *
 * Streaming calls update live: the transaction appears in Chucker as soon as the server
 * sends its initial headers, and the response body grows as messages arrive.
 *
 * @param collector [ChuckerCollector] that stores and displays the intercepted transactions.
 * @param context Android context (unused at runtime, kept for API parity with the no-op variant).
 * @param maxContentLength Maximum body size in bytes to record. Bodies larger than this are
 *   truncated. Defaults to 250 000 bytes.
 * @param redactHeaders Header names whose values should be replaced with `**REDACTED**`.
 */
public class ChuckerGrpcInterceptor private constructor(
    private val collector: ChuckerCollector,
    private val maxContentLength: Long,
    private val headersToRedact: Set<String>,
) : ClientInterceptor {
    public constructor(
        collector: ChuckerCollector,
        @Suppress("UNUSED_PARAMETER") context: Context,
        maxContentLength: Long = 250_000L,
        redactHeaders: Set<String> = emptySet(),
    ) : this(collector, maxContentLength, redactHeaders.map { it.lowercase() }.toSet())

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        val transaction = HttpTransaction()
        transaction.requestDate = System.currentTimeMillis()
        transaction.method = method.type.name
        transaction.protocol = "gRPC"

        val authority = next.authority() ?: "unknown"
        transaction.host = authority.substringBefore(":")
        transaction.path = "/${method.fullMethodName}"
        transaction.scheme = if (authority.substringAfterLast(':').toIntOrNull() == HTTPS_PORT) "https" else "http"
        transaction.url = "${transaction.scheme}://$authority${transaction.path}"

        // Guards to ensure onRequestSent is called exactly once before any onResponseReceived.
        val transactionStarted = AtomicBoolean(false)

        val requestBodyLock = Any()
        val requestBody = StringBuilder()
        val requestBodySize = AtomicLong(0L)

        val responseBodyLock = Any()
        val responseBody = StringBuilder()
        val responseBodySize = AtomicLong(0L)

        fun ensureStarted() {
            if (transactionStarted.compareAndSet(false, true)) {
                collector.onRequestSent(transaction)
            }
        }

        fun updateRequestBody() {
            val body = synchronized(requestBodyLock) { requestBody.toString() }
            val size = requestBodySize.get()
            transaction.requestBody = body.truncatedTo(size, maxContentLength)
            transaction.requestPayloadSize = size
        }

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions),
        ) {
            override fun start(
                responseListener: Listener<RespT>,
                headers: Metadata,
            ) {
                transaction.setRequestHeaders(headers.toHttpHeaders(headersToRedact))
                transaction.requestContentType = "application/grpc"

                super.start(
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                        responseListener,
                    ) {
                        override fun onHeaders(responseHeaders: Metadata) {
                            transaction.responseDate = System.currentTimeMillis()
                            transaction.setResponseHeaders(responseHeaders.toHttpHeaders(headersToRedact))
                            transaction.responseContentType = "application/grpc"
                            // Insert the transaction so it appears in Chucker immediately.
                            ensureStarted()
                            super.onHeaders(responseHeaders)
                        }

                        override fun onMessage(message: RespT) {
                            val text = message.toString()
                            val size = responseBodySize.addAndGet(text.length.toLong())
                            if (size <= maxContentLength) {
                                synchronized(responseBodyLock) { responseBody.append(text) }
                            }
                            // Live update: refresh the response body after each message.
                            val body = synchronized(responseBodyLock) { responseBody.toString() }
                            transaction.responseBody = body.truncatedTo(responseBodySize.get(), maxContentLength)
                            transaction.responsePayloadSize = responseBodySize.get()
                            collector.onResponseReceived(transaction)
                            super.onMessage(message)
                        }

                        override fun onClose(
                            status: Status,
                            trailers: Metadata,
                        ) {
                            transaction.responseCode = status.code.value()
                            transaction.responseMessage =
                                buildString {
                                    append(status.code.name)
                                    status.description?.let { append(" ($it)") }
                                }
                            status.cause?.let { transaction.error = it.toString() }
                            transaction.appendResponseTrailers(trailers.toHttpHeaders(headersToRedact))
                            transaction.tookMs =
                                System.currentTimeMillis() - (transaction.requestDate ?: System.currentTimeMillis())

                            // Ensure the transaction was started even on immediate errors
                            // (where onHeaders was never called).
                            ensureStarted()
                            collector.onResponseReceived(transaction)
                            super.onClose(status, trailers)
                        }
                    },
                    headers,
                )
            }

            override fun sendMessage(message: ReqT) {
                val text = message.toString()
                val size = requestBodySize.addAndGet(text.length.toLong())
                if (size <= maxContentLength) {
                    synchronized(requestBodyLock) { requestBody.append(text) }
                }
                // Keep the visible request body current for bidi-streaming calls
                // where responses (and thus onRequestSent) may have already fired.
                if (transactionStarted.get()) {
                    updateRequestBody()
                    collector.onResponseReceived(transaction)
                }
                super.sendMessage(message)
            }

            override fun halfClose() {
                // Client finished sending — capture the final request body.
                updateRequestBody()
                // If server headers haven't arrived yet (e.g. slow server), ensure
                // the transaction is in the DB so the request side is visible.
                ensureStarted()
                collector.onResponseReceived(transaction)
                super.halfClose()
            }
        }
    }

    private fun Metadata.toHttpHeaders(redact: Set<String>): List<HttpHeader> =
        keys().flatMap { key ->
            val isBinary = key.endsWith(Metadata.BINARY_HEADER_SUFFIX)
            val values: Iterable<String> =
                if (isBinary) {
                    getAll(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER))
                        ?.map { it.toString(Charsets.UTF_8) + " (binary)" }
                        ?: emptyList()
                } else {
                    getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)) ?: emptyList()
                }
            val displayKey = key.lowercase()
            values.map { HttpHeader(key, if (redact.contains(displayKey)) "**REDACTED**" else it) }
        }

    private fun HttpTransaction.appendResponseTrailers(trailers: List<HttpHeader>) {
        if (trailers.isEmpty()) return
        val type = object : TypeToken<List<HttpHeader>>() {}.type
        val existing =
            JsonConverter.instance.fromJson<List<HttpHeader>>(responseHeaders ?: "[]", type)
                ?: emptyList()
        responseHeaders =
            JsonConverter.instance.toJson(
                existing + trailers.map { HttpHeader("(Trailer) ${it.name}", it.value) },
            )
    }

    private fun String.truncatedTo(
        actualSize: Long,
        max: Long,
    ): String = if (actualSize > max) take(max.toInt()) + "\n\n--- (truncated)" else this

    private companion object {
        const val HTTPS_PORT = 443
    }
}
