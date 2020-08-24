package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName

internal data class PostData(
    @SerializedName("size") val size: Long,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("text") val text: String
) {
    companion object {
        fun responsePostData(transaction: HttpTransaction): PostData? {
            if (transaction.responsePayloadSize == null || !transaction.isResponseBodyPlainText) return null

            return PostData(
                size = transaction.responsePayloadSize ?: 0,
                mimeType = transaction.responseContentType ?: "text",
                text = transaction.responseBody ?: ""
            )
        }

        fun requestPostData(transaction: HttpTransaction): PostData? {
            if (transaction.requestPayloadSize == null || !transaction.isRequestBodyPlainText) return null
            return PostData(
                size = transaction.requestPayloadSize ?: 0,
                mimeType = transaction.requestContentType ?: "text",
                text = transaction.requestBody ?: ""
            )
        }
    }
}
