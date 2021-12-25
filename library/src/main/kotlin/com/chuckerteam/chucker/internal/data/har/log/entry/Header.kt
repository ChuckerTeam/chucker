package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#headers
// http://www.softwareishard.com/blog/har-12-spec/#headers
internal data class Header(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(header: HttpHeader) : this(
        name = header.name,
        value = header.value
    )
}
