package com.chuckerteam.chucker.internal.ui

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import java.text.DateFormat

/** Display-ready transaction data for a row in the transaction list. */
@Suppress("LongParameterList")
internal data class TransactionUi(
    val id: Long,
    val requestDate: String,
    val host: String?,
    val path: String,
    val isSsl: Boolean,
    val responseCode: Int?,
    val status: HttpTransaction.Status,
    val duration: String?,
    val totalSize: String,
    val graphQlOperationName: String?,
    val isGraphQl: Boolean,
)

internal fun HttpTransactionTuple.toTransactionUi(): TransactionUi =
    TransactionUi(
        id = id,
        requestDate = DateFormat.getTimeInstance().format(requestDate),
        host = host,
        path = "$method ${getFormattedPath(encode = false)}",
        isSsl = isSsl,
        responseCode = responseCode,
        status = status,
        duration = durationString,
        totalSize = totalSizeString,
        graphQlOperationName = graphQlOperationName,
        isGraphQl = graphQlDetected,
    )
