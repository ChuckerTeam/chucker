package com.chuckerteam.chucker.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember")
public class ChuckerInterceptor @JvmOverloads constructor(
    context: Context,
    collector: Any? = null,
    maxContentLength: Any? = null,
    headersToRedact: Any? = null,
    alwaysReadResponseBody: Any? = null,
) : Interceptor {

    public fun redactHeaders(vararg names: String): ChuckerInterceptor {
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }

    /**
     * No-op implementation.
     */
    public class Builder(private val context: Context) {
        public fun collector(collector: ChuckerCollector): Builder = this

        public fun maxContentLength(length: Long): Builder = this

        public fun redactHeaders(headerNames: Iterable<String>): Builder = this

        public fun redactHeaders(vararg headerNames: String): Builder = this

        public fun alwaysReadResponseBody(enable: Boolean): Builder = this

        public fun build(): ChuckerInterceptor = ChuckerInterceptor(context)
    }
}
