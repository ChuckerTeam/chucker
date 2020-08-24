package com.chuckerteam.chucker.internal.data.har

import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl

internal data class QueryString(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
) {
    companion object {
        fun fromUrl(url: HttpUrl): List<QueryString> {
            val querySize = url.querySize()
            return (0 until querySize).map { index ->
                QueryString(
                    name = url.queryParameterName(index),
                    value = url.queryParameterValue(index)
                )
            }
        }
    }
}
