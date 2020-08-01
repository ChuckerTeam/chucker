package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Header(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
