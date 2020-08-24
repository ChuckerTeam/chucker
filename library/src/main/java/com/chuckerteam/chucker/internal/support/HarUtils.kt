package com.chuckerteam.chucker.internal.support

import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.BuildConfig
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.Creator
import com.chuckerteam.chucker.internal.data.har.Entry
import com.chuckerteam.chucker.internal.data.har.Har
import com.chuckerteam.chucker.internal.data.har.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// http://www.softwareishard.com/blog/har-12-spec/
// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md
internal object HarUtils {
    suspend fun harStringFromTransactions(
        transactions: List<HttpTransaction>
    ): String = withContext(Dispatchers.Default) {
        JsonConverter.nonNullSerializerInstance.toJson(fromHttpTransactions(transactions))
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
