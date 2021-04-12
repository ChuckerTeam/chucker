package com.chuckerteam.chucker.sample

import okhttp3.Interceptor

enum class InterceptorType {
    APPLICATION,
    NETWORK,
    ;

    interface Provider {
        val value: InterceptorType
    }
}

fun Interceptor.activeForType(
    activeType: InterceptorType,
    typeProvider: InterceptorType.Provider,
) = Interceptor { chain ->
    if (activeType == typeProvider.value) {
        intercept(chain)
    } else {
        chain.proceed(chain.request())
    }
}
