package com.chuckerteam.chucker.internal.data.entity

import com.squareup.moshi.Json

internal data class HttpHeader(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: String
)
