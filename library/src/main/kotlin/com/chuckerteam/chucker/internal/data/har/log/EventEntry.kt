package com.chuckerteam.chucker.internal.data.har.log

import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.google.gson.annotations.SerializedName

internal data class EventEntry(
    @SerializedName("title") val title: String? = null,
    @SerializedName("payload") val payload: String? = null,
    @SerializedName("time") var time: Long? = 0
) : Entry{
    constructor(transaction: EventTransaction) : this(
        title = transaction.title,
        payload = transaction.payload,
        time = transaction.receivedDate
    )
}
