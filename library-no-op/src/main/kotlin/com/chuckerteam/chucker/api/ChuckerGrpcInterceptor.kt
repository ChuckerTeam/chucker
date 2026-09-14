package com.chuckerteam.chucker.api

import android.content.Context
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.MethodDescriptor

/**
 * No-op implementation of [ChuckerGrpcInterceptor] for use in release builds.
 *
 * All parameters are accepted for source-level API compatibility with the debug variant
 * but have no effect at runtime; calls pass straight through to the channel.
 */
public class ChuckerGrpcInterceptor private constructor(
    @Suppress("UNUSED_PARAMETER") private val collector: ChuckerCollector,
    @Suppress("UNUSED_PARAMETER") private val maxContentLength: Long,
    @Suppress("UNUSED_PARAMETER") private val headersToRedact: Set<String>,
) : ClientInterceptor {
    public constructor(
        collector: ChuckerCollector,
        @Suppress("UNUSED_PARAMETER") context: Context,
        maxContentLength: Long = 250_000L,
        redactHeaders: Set<String> = emptySet(),
    ) : this(collector, maxContentLength, redactHeaders)

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> = next.newCall(method, callOptions)
}
