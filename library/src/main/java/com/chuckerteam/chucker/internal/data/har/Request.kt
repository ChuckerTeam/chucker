package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl

internal data class Request(
    @SerializedName("method") val method: String,
    @SerializedName("url") val url: String,
    @SerializedName("httpVersion") val httpVersion: String,
    @SerializedName("cookies") val cookies: List<Cookie>,
    @SerializedName("headers") val headers: List<Header>,
    @SerializedName("queryString") val queryString: List<QueryString>,
    @SerializedName("postData") val postData: PostData?,
    @SerializedName("headersSize") val headersSize: Int,
    @SerializedName("bodySize") val bodySize: Long
) {
    companion object {
        fun fromHttpTransaction(transaction: HttpTransaction): Request? {
            if (transaction.requestDate == null) {
                return null
            }
            return Request(
                method = transaction.method ?: "",
                url = transaction.url ?: "",
                httpVersion = transaction.protocol ?: "",
                cookies = emptyList(),
                headers = transaction.getParsedRequestHeaders()?.map { Header(it.name, it.value) } ?: emptyList(),
                queryString = QueryString.fromUrl(HttpUrl.get(transaction.url ?: "")),
                postData = PostData.requestPostData(transaction),
                headersSize = transaction.requestHeaders?.length ?: 0,
                bodySize = transaction.requestPayloadSize ?: 0
            )
        }
    }
}
