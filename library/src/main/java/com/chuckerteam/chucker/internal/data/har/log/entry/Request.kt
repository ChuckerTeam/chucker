package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.internal.data.har.log.entry.request.QueryString
import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl.Companion.toHttpUrl

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#request
// http://www.softwareishard.com/blog/har-12-spec/#request
internal data class Request(
    @SerializedName("method") val method: String,
    @SerializedName("url") val url: String,
    @SerializedName("httpVersion") val httpVersion: String,
    @SerializedName("cookies") val cookies: List<Any> = emptyList(),
    @SerializedName("headers") val headers: List<Header>,
    @SerializedName("queryString") val queryString: List<QueryString>,
    @SerializedName("postData") val postData: PostData? = null,
    @SerializedName("headersSize") val headersSize: Long,
    @SerializedName("bodySize") val bodySize: Long,
    @SerializedName("totalSize") val totalSize: Long,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        method = transaction.method ?: "",
        url = transaction.url ?: "",
        httpVersion = transaction.protocol ?: "",
        headers = transaction.getParsedRequestHeaders()?.map { Header(it) } ?: emptyList(),
        queryString = transaction.url?.let { QueryString.fromUrl(it.toHttpUrl()) } ?: emptyList(),
        postData = transaction.requestPayloadSize?.run { PostData(transaction) },
        headersSize = transaction.requestHeadersSize ?: 0,
        bodySize = transaction.requestPayloadSize ?: 0,
        totalSize = transaction.getRequestTotalSize()
    )
}
