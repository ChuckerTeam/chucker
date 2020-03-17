package com.chuckerteam.chucker.internal.support

import com.squareup.moshi.Moshi

internal object JsonConverter {

    val instance: Moshi by lazy {
        Moshi.Builder()
            .build()
    }
}
