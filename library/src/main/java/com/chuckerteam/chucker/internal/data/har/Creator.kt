package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Creator(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String
)
