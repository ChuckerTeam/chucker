package com.chuckerteam.chucker.internal.data.har.log.page

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#pagetimings
internal data class PageTimings(
    @SerializedName("onContentLoad") val onContentLoad: Long = -1,
    @SerializedName("onLoad") val onLoad: Long = -1,
    @SerializedName("comment") val comment: String? = null
)
