package com.chuckerteam.chucker.internal.data.har.log.entry.request

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.entry.request.postdata.Params
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#postdata
// text and params fields are mutually exclusive.
internal data class PostData(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("params") val params: Params?,
    @SerializedName("text") val text: String?,
    @SerializedName("comment") val comment: String? = null,
) {
    constructor(transaction: HttpTransaction) : this(
        mimeType = transaction.requestContentType ?: "x-unknown",
        params = if (transaction.isRequestBodyEncoded) null else null,
        text = if (transaction.isRequestBodyEncoded) null else transaction.requestBody
    )
}
