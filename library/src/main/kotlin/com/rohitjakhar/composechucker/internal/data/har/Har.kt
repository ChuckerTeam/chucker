package com.rohitjakhar.composechucker.internal.data.har

import com.rohitjakhar.composechucker.internal.data.entity.HttpTransaction
import com.rohitjakhar.composechucker.internal.data.har.log.Creator
import com.google.gson.annotations.SerializedName

internal data class Har(
    @SerializedName("log") val log: Log
) {
    constructor(transactions: List<HttpTransaction>, creator: Creator) : this(
        log = Log(transactions, creator)
    )
}
