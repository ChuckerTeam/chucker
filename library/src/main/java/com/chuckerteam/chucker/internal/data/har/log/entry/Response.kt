package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#response
// http://www.softwareishard.com/blog/har-12-spec/#response
internal data class Response(
    @SerializedName("status") val status: Int,
    @SerializedName("statusText") val statusText: String,
    @SerializedName("httpVersion") val httpVersion: String,
    @SerializedName("cookies") val cookies: List<Any> = emptyList(),
    @SerializedName("headers") val headers: List<Header>,
    @SerializedName("content") val content: Content? = null,
    @SerializedName("redirectURL") val redirectUrl: String = "",
    @SerializedName("headersSize") val headersSize: Long,
    @SerializedName("bodySize") val bodySize: Long,
    @SerializedName("totalSize") val totalSize: Long,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        status = transaction.responseCode ?: 0,
        statusText = transaction.responseMessage ?: "",
        httpVersion = transaction.protocol ?: "",
        headers = transaction.getParsedResponseHeaders()?.map { Header(it) } ?: emptyList(),
        content = transaction.responsePayloadSize?.run { Content(transaction) },
        headersSize = transaction.responseHeadersSize ?: 0,
        bodySize = transaction.getHarResponseBodySize(),
        totalSize = transaction.getResponseTotalSize()
    )
}
