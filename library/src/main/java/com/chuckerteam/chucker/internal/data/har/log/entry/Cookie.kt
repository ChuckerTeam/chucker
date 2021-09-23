package com.chuckerteam.chucker.internal.data.har.log.entry

import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#cookies
internal data class Cookie(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("path") val path: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("httpOnly") val httpOnly: Boolean?,
    @SerializedName("secure") val secure: Boolean?,
    @SerializedName("comment") val comment: String? = null
)
