package com.chuckerteam.chucker.internal.support

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import java.util.Date

internal object JsonConverter {

    val instance: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date::class.java, DateTypeAdapter())
            .create()
    }
}
