package com.chuckerteam.chucker.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient

internal enum class ClientFactory {
    APPLICATION {
        override fun create(interceptor: Interceptor): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
        }
    },
    NETWORK {
        override fun create(interceptor: Interceptor): OkHttpClient {
            return OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build()
        }
    };

    abstract fun create(interceptor: Interceptor): OkHttpClient
}
