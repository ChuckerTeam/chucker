package com.chuckerteam.chucker.internal.data.har.log.entry.request.postdata

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#params
// http://www.softwareishard.com/blog/har-12-spec/#params
internal data class Params(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("contentType") val contentType: String? = null,
    @SerializedName("comment") val comment: String? = null
)
