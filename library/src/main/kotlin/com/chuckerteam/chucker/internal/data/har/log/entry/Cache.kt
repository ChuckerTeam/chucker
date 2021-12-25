package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.har.log.entry.cache.SecondaryRequest
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#cache
// http://www.softwareishard.com/blog/har-12-spec/#cache
internal data class Cache(
    @SerializedName("afterRequest") val afterRequest: SecondaryRequest? = null,
    @SerializedName("beforeRequest") val beforeRequest: SecondaryRequest? = null,
    @SerializedName("comment") val comment: String? = null
)
