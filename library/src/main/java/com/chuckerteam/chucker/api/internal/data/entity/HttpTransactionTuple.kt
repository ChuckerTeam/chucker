package com.chuckerteam.chucker.api.internal.data.entity

import androidx.room.ColumnInfo
import com.chuckerteam.chucker.api.internal.support.FormatUtils

/**
 * A subset of [HttpTransaction] to perform faster Read operations on the Repository.
 * This Tuple is good to be used on List or Preview interfaces.
 */
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
    val isSsl: Boolean get() = scheme?.toLowerCase() == "https"

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
}
