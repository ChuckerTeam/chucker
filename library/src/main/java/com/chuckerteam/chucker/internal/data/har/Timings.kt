package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Timings(
    @SerializedName("send") val send: Long,
    @SerializedName("wait") val wait: Long,
    @SerializedName("receive") val receive: Long
)
