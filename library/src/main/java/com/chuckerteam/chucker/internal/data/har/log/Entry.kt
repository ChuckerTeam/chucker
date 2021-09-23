package com.chuckerteam.chucker.internal.data.har.log

import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.entry.Cache
import com.chuckerteam.chucker.internal.data.har.log.entry.Request
import com.chuckerteam.chucker.internal.data.har.log.entry.Response
import com.chuckerteam.chucker.internal.data.har.log.entry.Timings
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md#entries
internal data class Entry(
    @SerializedName("pageref") val pageref: String?,
    @SerializedName("startedDateTime") val startedDateTime: String,
    @SerializedName("time") var time: Long,
    @SerializedName("request") val request: Request,
    @SerializedName("response") val response: Response,
    @SerializedName("cache") val cache: Cache,
    @SerializedName("timings") val timings: Timings,
    @SerializedName("serverIPAddress") val serverIPAddress: String?,
    @SerializedName("connection") val connection: String?,
    @SerializedName("comment") val comment: String? = null
) {
    constructor(transaction: HttpTransaction) : this(
        pageref = null,
        startedDateTime = transaction.requestDate.harFormatted(),
        time = -1,
        request = Request(transaction),
        response = Response(transaction),
        cache = Cache(),
        timings = Timings(transaction),
        serverIPAddress = null,
        connection = null
    ) {
        time = timings.getTime()
    }

    @VisibleForTesting
    object DateFormat : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

        private fun Long?.harFormatted(): String {
            val date = if (this == null) Date() else Date(this)
            return DateFormat.get()?.format(date) ?: ""
        }
    }
}
