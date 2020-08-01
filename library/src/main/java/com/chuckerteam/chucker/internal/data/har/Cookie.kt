package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Cookie(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
