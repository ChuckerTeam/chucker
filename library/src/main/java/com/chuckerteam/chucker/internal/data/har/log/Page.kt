package com.chuckerteam.chucker.internal.data.har.log

import com.chuckerteam.chucker.internal.data.har.log.page.PageTimings
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#pages
// http://www.softwareishard.com/blog/har-12-spec/#pages
internal data class Page(
    @SerializedName("startedDateTime") val startedDateTime: String,
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("pageTimings") val pageTimings: PageTimings,
    @SerializedName("comment") val comment: String? = null
)
