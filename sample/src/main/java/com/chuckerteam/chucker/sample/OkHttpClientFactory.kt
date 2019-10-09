package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.sample.ChuckerInterceptorFactory.Companion.chuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object OkHttpClientFactory {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Add a ChuckerInterceptor instance to your OkHttp client
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
}
