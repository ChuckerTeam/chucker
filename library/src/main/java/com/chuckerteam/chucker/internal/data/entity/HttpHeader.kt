package com.chuckerteam.chucker.internal.data.entity

import com.google.gson.annotations.SerializedName

internal data class HttpHeader(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
