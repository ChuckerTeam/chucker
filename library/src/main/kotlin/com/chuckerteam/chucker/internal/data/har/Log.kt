package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.har.log.*
import com.chuckerteam.chucker.internal.data.har.log.Browser
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.internal.data.har.log.EventEntry
import com.chuckerteam.chucker.internal.data.har.log.HttpEntry
import com.chuckerteam.chucker.internal.data.har.log.Page
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#log
// http://www.softwareishard.com/blog/har-12-spec/#log
internal data class Log(
    @SerializedName("version") val version: String = "1.2",
    @SerializedName("creator") val creator: Creator,
    @SerializedName("browser") val browser: Browser? = null,
    @SerializedName("pages") val pages: List<Page>? = null,
    @SerializedName("entries") val entries: List<Entry>,
    @SerializedName("comment") val comment: String? = null,
) {
    constructor(transactions: List<Transaction>, creator: Creator) : this(
        creator = creator,
        entries = transactions.map {
            return@map when(it) {
                is EventTransaction -> EventEntry(it)
                is HttpTransaction -> HttpEntry(it)
            }
        }
    )
}
