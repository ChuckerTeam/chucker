package com.chuckerteam.chucker

import com.google.gson.Gson
import com.google.gson.GsonBuilder

public object GsonInstance {
    private var gson: Gson? = null
    public fun get(): Gson? {
        if (gson == null) {
            gson = GsonBuilder().setLenient().setPrettyPrinting().create()
        }
        return gson
    }
}
