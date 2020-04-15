package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.support.FormattedUrl
import okhttp3.HttpUrl

/**
 * A subset of [HttpTransaction] to perform faster Read operations on the Repository.
 * This Tuple is good to be used on List or Preview interfaces.
 */
@Suppress("LongParameterList")
internal class HttpTransactionTuple(
    @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "requestDate") var requestDate: Long?,
    @ColumnInfo(name = "tookMs") var tookMs: Long?,
    @ColumnInfo(name = "protocol") var protocol: String?,
    @ColumnInfo(name = "method") var method: String?,
    @ColumnInfo(name = "host") var host: String?,
    @ColumnInfo(name = "path") var path: String?,
    @ColumnInfo(name = "scheme") var scheme: String?,
    @ColumnInfo(name = "responseCode") var responseCode: Int?,
    @ColumnInfo(name = "requestContentLength") var requestContentLength: Long?,
    @ColumnInfo(name = "responseContentLength") var responseContentLength: Long?,
    @ColumnInfo(name = "error") var error: String?
) {
    val isSsl: Boolean get() = scheme.equals("https", ignoreCase = true)

    val status: HttpTransaction.Status
        get() = when {
            error != null -> HttpTransaction.Status.Failed
            responseCode == null -> HttpTransaction.Status.Requested
            else -> HttpTransaction.Status.Complete
        }

    val durationString: String? get() = tookMs?.let { "$it ms" }

    val totalSizeString: String
        get() {
            val reqBytes = requestContentLength ?: 0
            val resBytes = responseContentLength ?: 0
            return formatBytes(reqBytes + resBytes)
        }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    fun getFormattedPath(encode: Boolean): String {
        val path = this.path ?: return ""

        // Create dummy URL since there is no data in this class to get it from
        // and we are only interested in a formatted path with query.
        val dummyUrl = "https://www.example.com$path"

        val httpUrl = HttpUrl.parse(dummyUrl) ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).pathWithQuery
    }
}
