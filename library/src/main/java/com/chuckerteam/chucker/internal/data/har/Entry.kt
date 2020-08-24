package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class Entry(
    @SerializedName("startedDateTime") val startedDateTime: String,
    @SerializedName("time") val time: Long,
    @SerializedName("request") val request: Request?,
    @SerializedName("response") val response: Response?
) {
    companion object {
        val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

        fun fromHttpTransaction(transaction: HttpTransaction) = Entry(
            startedDateTime = transaction.requestDate.harFormatted(),
            time = transaction.tookMs ?: 0,
            request = Request.fromHttpTransaction(transaction),
            response = Response.fromHttpTransaction(transaction)
        )

        private fun Long?.harFormatted(): String {
            val date = if (this == null) {
                Instant.now().atZone(ZoneId.systemDefault())
            } else {
                Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
            }
            return DATE_FORMAT.format(date)
        }
    }
}
