package com.chuckerteam.chucker.internal.data.har.log.entry

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName
import kotlin.math.max

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#timings
internal data class Timings(
    @SerializedName("blocked") val blocked: Long,
    @SerializedName("dns") val dns: Long,
    @SerializedName("ssl") val ssl: Long,
    @SerializedName("connect") val connect: Long,
    @SerializedName("send") val send: Long,
    @SerializedName("wait") val wait: Long,
    @SerializedName("receive") val receive: Long,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        blocked = -1,
        dns = -1,
        ssl = -1,
        connect = -1,
        send = 0,
        wait = 0,
        receive = transaction.tookMs ?: 0,
    )

    fun getTime(): Long {
        return max(0, blocked) + max(0, dns) + max(0, connect) +
            send + wait + receive + max(0, ssl)
    }
}
