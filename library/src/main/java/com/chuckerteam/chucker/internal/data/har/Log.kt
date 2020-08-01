package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Log(
    @SerializedName("version") val version: String,
    @SerializedName("creator") val creator: Creator,
    @SerializedName("entries") val entries: List<Entry>
)
