package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.google.gson.annotations.SerializedName

internal data class Har(
    @SerializedName("log") val log: Log
) {
    constructor(transactions: List<Transaction>, creator: Creator) : this(
        log = Log(transactions, creator)
    )
}
