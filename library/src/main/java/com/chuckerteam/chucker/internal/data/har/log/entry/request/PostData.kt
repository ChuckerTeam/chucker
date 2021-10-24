package com.chuckerteam.chucker.internal.data.har.log.entry.request

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.entry.request.postdata.Params
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#postdata
// http://www.softwareishard.com/blog/har-12-spec/#postData
// text and params fields are mutually exclusive.
internal data class PostData(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("params") val params: Params? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("comment") val comment: String? = null,
) {
    constructor(transaction: HttpTransaction) : this(
        mimeType = transaction.requestContentType ?: "application/octet-stream",
        text = transaction.requestBody
    )
}
