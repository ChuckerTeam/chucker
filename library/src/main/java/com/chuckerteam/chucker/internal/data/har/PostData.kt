package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName

internal data class PostData(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("text") val text: String
) {
    companion object {
        fun responsePostData(transaction: HttpTransaction): PostData? {
            if (transaction.responseContentType == null || transaction.responseBody == null) return null
            return PostData(
                mimeType = transaction.responseContentType!!,
                text = transaction.responseBody!!
            )
        }

        fun requestPostData(transaction: HttpTransaction): PostData? {
            if (transaction.requestContentType == null || transaction.requestBody == null) return null
            return PostData(
                mimeType = transaction.requestContentType!!,
                text = transaction.requestBody!!
            )
        }
    }
}
