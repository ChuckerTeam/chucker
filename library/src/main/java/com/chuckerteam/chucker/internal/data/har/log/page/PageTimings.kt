package com.chuckerteam.chucker.internal.data.har.log.page

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#pagetimings
// http://www.softwareishard.com/blog/har-12-spec/#pageTimings
internal data class PageTimings(
    @SerializedName("onContentLoad") val onContentLoad: Long? = null,
    @SerializedName("onLoad") val onLoad: Long? = null,
    @SerializedName("comment") val comment: String? = null
)
