package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

internal class TransactionDetailsSharable(
    private val transaction: HttpTransaction,
    private val encodeUrls: Boolean,
) : Sharable {
    override fun toSharableContent(context: Context) = buildString {
        append("${context.getString(R.string.chucker_url)}: ${transaction.getFormattedUrl(encodeUrls)}\n")
        append("${context.getString(R.string.chucker_method)}: ${transaction.method}\n")
        append("${context.getString(R.string.chucker_protocol)}: ${transaction.protocol}\n")
        append("${context.getString(R.string.chucker_status)}: ${transaction.status}\n")
        append("${context.getString(R.string.chucker_response)}: ${transaction.responseSummaryText}\n")
        val isSsl = if (transaction.isSsl) R.string.chucker_yes else R.string.chucker_no
        append("${context.getString(R.string.chucker_ssl)}: ${context.getString(isSsl)}\n")
        append('\n')
        append("${context.getString(R.string.chucker_request_time)}: ${transaction.requestDateString}\n")
        append("${context.getString(R.string.chucker_response_time)}: ${transaction.responseDateString}\n")
        append("${context.getString(R.string.chucker_duration)}: ${transaction.durationString}\n")
        append('\n')
        append("${context.getString(R.string.chucker_request_size)}: ${transaction.requestSizeString}\n")
        append("${context.getString(R.string.chucker_response_size)}: ${transaction.responseSizeString}\n")
        append("${context.getString(R.string.chucker_total_size)}: ${transaction.totalSizeString}\n")
        append('\n')
        append("---------- ${context.getString(R.string.chucker_request)} ----------\n\n")

        var headers = FormatUtils.formatHeaders(transaction.getParsedRequestHeaders(), false)

        if (headers.isNotBlank()) {
            append(headers)
            append('\n')
        }

        append(
            if (transaction.isRequestBodyPlainText) {
                if (transaction.requestBody.isNullOrBlank()) {
                    context.getString(R.string.chucker_body_empty)
                } else {
                    transaction.getFormattedRequestBody()
                }
            } else {
                context.getString(R.string.chucker_body_omitted)
            }
        )

        append("\n\n")
        append("---------- ${context.getString(R.string.chucker_response)} ----------\n\n")

        headers = FormatUtils.formatHeaders(transaction.getParsedResponseHeaders(), false)

        if (headers.isNotBlank()) {
            append(headers)
            append('\n')
        }

        append(
            if (transaction.isResponseBodyPlainText) {
                if (transaction.responseBody.isNullOrBlank()) {
                    context.getString(R.string.chucker_body_empty)
                } else {
                    transaction.getFormattedResponseBody()
                }
            } else {
                context.getString(R.string.chucker_body_omitted)
            }
        )
    }
}
