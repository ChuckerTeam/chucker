package com.chuckerteam.chucker.internal.data.har.log.entry.response

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#content
// http://www.softwareishard.com/blog/har-12-spec/#content
internal data class Content(
    @SerializedName("size") val size: Long? = null,
    @SerializedName("compression") val compression: Int? = null,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("encoding") val encoding: String? = null,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        size = transaction.responsePayloadSize,
        mimeType = transaction.responseContentType ?: "application/octet-stream",
        text = transaction.responseBody,
    )
}
