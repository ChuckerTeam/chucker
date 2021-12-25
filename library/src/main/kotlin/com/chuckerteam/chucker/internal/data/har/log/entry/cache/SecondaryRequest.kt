package com.chuckerteam.chucker.internal.data.har.log.entry.cache

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#beforerequest--afterrequest
// http://www.softwareishard.com/blog/har-12-spec/#cache
internal data class SecondaryRequest(
    @SerializedName("expires") val expires: String? = null,
    @SerializedName("lastAccess") val lastAccess: String,
    @SerializedName("eTag") val eTag: String,
    @SerializedName("hitCount") val hitCount: Int,
    @SerializedName("comment") val comment: String? = null
)
