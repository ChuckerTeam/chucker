package com.rohitjakhar.composechucker.internal.data.har

import com.rohitjakhar.composechucker.internal.data.entity.HttpTransaction
import com.rohitjakhar.composechucker.internal.data.har.log.Browser
import com.rohitjakhar.composechucker.internal.data.har.log.Creator
import com.rohitjakhar.composechucker.internal.data.har.log.Entry
import com.rohitjakhar.composechucker.internal.data.har.log.Page
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#log
// http://www.softwareishard.com/blog/har-12-spec/#log
internal data class Log(
    @SerializedName("version") val version: String = "1.2",
    @SerializedName("creator") val creator: Creator,
    @SerializedName("browser") val browser: Browser? = null,
    @SerializedName("pages") val pages: List<Page>? = null,
    @SerializedName("entries") val entries: List<Entry>,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transactions: List<HttpTransaction>, creator: Creator) : this(
        creator = creator,
        entries = transactions.map { Entry(it) }
    )
}
