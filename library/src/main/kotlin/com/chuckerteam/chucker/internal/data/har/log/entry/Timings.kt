package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#timings
// http://www.softwareishard.com/blog/har-12-spec/#timings
internal data class Timings(
    @SerializedName("blocked") val blocked: Long? = null,
    @SerializedName("dns") val dns: Long? = null,
    @SerializedName("ssl") val ssl: Long? = null,
    @SerializedName("connect") val connect: Long = 0,
    @SerializedName("send") val send: Long = 0,
    @SerializedName("wait") val wait: Long,
    @SerializedName("receive") val receive: Long = 0,
    @SerializedName("comment") val comment: String = "The information described by this object is incomplete."
) {
    constructor(transaction: HttpTransaction) : this(
        wait = transaction.tookMs ?: 0,
    )

    fun getTime(): Long {
        return (blocked ?: 0) + (dns ?: 0) + (ssl ?: 0) + connect + send + wait + receive
    }
}
