package com.chuckerteam.chucker.internal.data.har

import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.BuildConfig
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.JsonConverter
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

// http://www.softwareishard.com/blog/har-12-spec/
// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md
internal data class Har(
    @SerializedName("log") val log: Log
) {
    object DateFormat : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }

    companion object {
        suspend fun harStringFromTransactions(transactions: List<HttpTransaction>): String = withContext(Dispatchers.Default) {
            JsonConverter.harInstance.toJson(fromHttpTransactions(transactions))
        }

        @VisibleForTesting fun fromHttpTransactions(transactions: List<HttpTransaction>): Har {
            return Har(
                log = Log(
                    version = "1.2",
                    creator = Creator(
                        name = BuildConfig.LIBRARY_PACKAGE_NAME,
                        version = BuildConfig.VERSION_NAME
                    ),
                    entries = transactions.map(Entry.Companion::fromHttpTransaction).filter { it.response != null }
                )
            )
        }
    }
}
