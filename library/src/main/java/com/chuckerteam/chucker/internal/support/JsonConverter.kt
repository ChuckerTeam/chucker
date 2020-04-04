package com.chuckerteam.chucker.internal.support

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal object JsonConverter {

    val instance: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()
    }
}
