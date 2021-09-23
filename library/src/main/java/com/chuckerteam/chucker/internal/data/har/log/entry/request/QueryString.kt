package com.chuckerteam.chucker.internal.data.har.log.entry.request

import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#querystring
internal data class QueryString(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("comment") val comment: String? = null
) {
    companion object {
        fun fromUrl(url: HttpUrl): List<QueryString> {
            val querySize = url.querySize
            return (0 until querySize).map { index ->
                QueryString(
                    name = url.queryParameterName(index),
                    value = url.queryParameterValue(index) ?: ""
                )
            }
        }
    }
}
