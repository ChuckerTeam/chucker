package com.chuckerteam.chucker.internal.data.har.log.entry.response

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#content
internal data class Content(
    @SerializedName("size") val size: Long?,
    @SerializedName("compression") val compression: Int?,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("text") val text: String?,
    @SerializedName("encoding") val encoding: String?,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        size = transaction.responsePayloadSize,
        compression = null,
        mimeType = transaction.responseContentType ?: "x-unknown",
        text = transaction.responseBody,
        encoding = if (transaction.isResponseBodyEncoded) null else null
    )
}
