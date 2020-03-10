package com.chuckerteam.chucker.internal.support

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal object JsonConverter {

    val instance: Moshi by lazy {
        Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
    }

}
