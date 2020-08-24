package com.chuckerteam.chucker.internal.data.har

import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class Entry(
    @SerializedName("startedDateTime") val startedDateTime: String,
    @SerializedName("time") val time: Long,
    @SerializedName("request") val request: Request?,
    @SerializedName("response") val response: Response?
) {
    @VisibleForTesting object DateFormat : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }

    companion object {
        fun fromHttpTransaction(transaction: HttpTransaction) = Entry(
            startedDateTime = transaction.requestDate.harFormatted(),
            time = transaction.tookMs ?: 0,
            request = Request.fromHttpTransaction(transaction),
            response = Response.fromHttpTransaction(transaction)
        )

        private fun Long?.harFormatted(): String {
            val date = if (this == null) Date() else Date(this)
            return DateFormat.get()?.format(date) ?: ""
        }
    }
}
