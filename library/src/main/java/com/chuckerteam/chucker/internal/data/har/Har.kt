package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName

internal data class Har(
    @SerializedName("log") val log: Log
)
