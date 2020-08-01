package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName
import java.util.Date

internal data class Entry(
    @SerializedName("startedDateTime") val startedDateTime: String,
    @SerializedName("time") val time: Long,
    @SerializedName("request") val request: Request?,
    @SerializedName("response") val response: Response?
) {
    companion object {
        fun fromHttpTransaction(transaction: HttpTransaction): Entry = Entry(
            startedDateTime = transaction.requestDate.harFormatted(),
            time = transaction.tookMs ?: 0,
            request = Request.fromHttpTransaction(transaction),
            response = Response.fromHttpTransaction(transaction)
        )

        private fun Long?.harFormatted(): String {
            val date = if (this == null) Date() else Date(this)
            return Har.DateFormat.get()!!.format(date)
        }
    }
}
