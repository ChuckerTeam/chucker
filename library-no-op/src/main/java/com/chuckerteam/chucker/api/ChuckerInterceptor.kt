package com.chuckerteam.chucker.api

import android.content.Context
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op implementation.
 */
class ChuckerInterceptor @JvmOverloads constructor(
    context: Context,
    collector: Any? = null,
    maxContentLength: Any? = null,
    headersToRedact: Any? = null
) : Interceptor {

    fun redactHeaders(vararg names: String): ChuckerInterceptor {
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }
}
