package com.chuckerteam.chucker.internal.data.har.log

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#browser
// http://www.softwareishard.com/blog/har-12-spec/#browser
internal data class Browser(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String,
    @SerializedName("comment") val comment: String? = null
)
