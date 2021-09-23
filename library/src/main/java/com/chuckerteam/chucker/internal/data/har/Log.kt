package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.Browser
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.internal.data.har.log.Page
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#log
internal data class Log(
    @SerializedName("version") val version: String,
    @SerializedName("creator") val creator: Creator,
    @SerializedName("browser") val browser: Browser?,
    @SerializedName("pages") val pages: List<Page>?,
    @SerializedName("entries") val entries: List<Entry>,
    @SerializedName("comment") val comment: String? = null,
) {
    constructor(transactions: List<HttpTransaction>, creator: Creator) : this(
        version = "1.2",
        creator = creator,
        browser = null,
        pages = null,
        entries = transactions.map { Entry(it) }
    )
}
