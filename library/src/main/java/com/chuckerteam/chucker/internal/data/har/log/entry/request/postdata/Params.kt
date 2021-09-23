package com.chuckerteam.chucker.internal.data.har.log.entry.request.postdata

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#params
internal data class Params(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String?,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("contentType") val contentType: String?,
    @SerializedName("comment") val comment: String? = null
)
