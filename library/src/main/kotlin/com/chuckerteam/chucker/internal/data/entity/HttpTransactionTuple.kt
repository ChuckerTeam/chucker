package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.support.FormattedUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * A subset of [HttpTransaction] to perform faster Read operations on the Repository.
 * This Tuple is good to be used on List or Preview interfaces.
 */
@Suppress("LongParameterList")
internal data class HttpTransactionTuple(
    @ColumnInfo(name = "id") override var id: Long,
    @ColumnInfo(name = "requestDate") var requestDate: Long?,
    @ColumnInfo(name = "tookMs") var tookMs: Long?,
    @ColumnInfo(name = "protocol") var protocol: String?,
    @ColumnInfo(name = "method") var method: String?,
    @ColumnInfo(name = "host") var host: String?,
    @ColumnInfo(name = "path") var path: String?,
    @ColumnInfo(name = "scheme") var scheme: String?,
    @ColumnInfo(name = "responseCode") var responseCode: Int?,
    @ColumnInfo(name = "requestPayloadSize") var requestPayloadSize: Long?,
    @ColumnInfo(name = "responsePayloadSize") var responsePayloadSize: Long?,
    @ColumnInfo(name = "error") var error: String?
) : Transaction {
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
            val reqBytes = requestPayloadSize ?: 0
            val resBytes = responsePayloadSize ?: 0
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

        val httpUrl = dummyUrl.toHttpUrlOrNull() ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).pathWithQuery
    }

    override val notificationText: String
        get() {
            return when (status) {
                HttpTransaction.Status.Failed -> " ! ! !  $method $path"
                HttpTransaction.Status.Requested -> " . . .  $method $path"
                else -> "$responseCode $method $path"
            }
        }
    override val time: Long
        get() = requestDate ?: 0

    override fun hasTheSameContent(other: Transaction?): Boolean {
        if (this === other) return true
        if (other == null) return false

        if (other !is HttpTransactionTuple) {
            return false
        }

        return (id == other.id) &&
            (requestDate == other.requestDate) &&
            (tookMs == other.tookMs) &&
            (protocol == other.protocol) &&
            (method == other.method) &&
            (host == other.host) &&
            (path == other.path) &&
            (scheme == other.scheme) &&
            (requestPayloadSize == other.requestPayloadSize) &&
            (responseCode == other.responseCode) &&
            (error == other.error) &&
            (responsePayloadSize == other.responsePayloadSize)
    }
}
