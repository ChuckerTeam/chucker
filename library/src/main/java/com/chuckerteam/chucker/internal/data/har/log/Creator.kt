package com.chuckerteam.chucker.internal.data.har.log

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#creator
internal data class Creator(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String,
    @SerializedName("comment") val comment: String? = null
)
