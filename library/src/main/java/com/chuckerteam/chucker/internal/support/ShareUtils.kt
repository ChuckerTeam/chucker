package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ShareUtils {

    suspend fun getStringFromTransactions(transactions: List<HttpTransaction>, context: Context): String {
        return withContext(Dispatchers.Default) {
            transactions.joinToString(
                separator = "\n${context.getString(R.string.chucker_export_separator)}\n",
                prefix = "${context.getString(R.string.chucker_export_prefix)}\n",
                postfix = "\n${context.getString(R.string.chucker_export_postfix)}\n"
            ) { getShareText(context, it, false) }
        }
    }

    fun getShareText(context: Context, transaction: HttpTransaction, encodeUrls: Boolean): String {
        var text = "${context.getString(R.string.chucker_url)}: ${transaction.getFormattedUrl(encodeUrls)}\n"
        text += "${context.getString(R.string.chucker_method)}: ${transaction.method}\n"
        text += "${context.getString(R.string.chucker_protocol)}: ${transaction.protocol}\n"
        text += "${context.getString(R.string.chucker_status)}: ${transaction.status}\n"
        text += "${context.getString(R.string.chucker_response)}: ${transaction.responseSummaryText}\n"
        text += "${context.getString(R.string.chucker_ssl)}: " +
            "${context.getString(if (transaction.isSsl) R.string.chucker_yes else R.string.chucker_no)}\n"
        text += "\n"
        text += "${context.getString(R.string.chucker_request_time)}: ${transaction.requestDateString}\n"
        text += "${context.getString(R.string.chucker_response_time)}: ${transaction.responseDateString}\n"
        text += "${context.getString(R.string.chucker_duration)}: ${transaction.durationString}\n"
        text += "\n"
        text += "${context.getString(R.string.chucker_request_size)}: ${transaction.requestSizeString}\n"
        text += "${context.getString(R.string.chucker_response_size)}: ${transaction.responseSizeString}\n"
        text += "${context.getString(R.string.chucker_total_size)}: ${transaction.totalSizeString}\n"
        text += "\n"
        text += "---------- ${context.getString(R.string.chucker_request)} ----------\n\n"

        var headers = FormatUtils.formatHeaders(transaction.getParsedRequestHeaders(), false)

        if (headers.isNotBlank()) {
            text += "${headers}\n"
        }

        text += if (transaction.isRequestBodyPlainText) {
            if (transaction.requestBody.isNullOrBlank()) {
                context.getString(R.string.chucker_body_empty)
            } else {
                transaction.getFormattedRequestBody()
            }
        } else {
            context.getString(R.string.chucker_body_omitted)
        }

        text += "\n\n"
        text += "---------- ${context.getString(R.string.chucker_response)} ----------\n\n"

        headers = FormatUtils.formatHeaders(transaction.getParsedResponseHeaders(), false)

        if (headers.isNotBlank()) {
            text += "${headers}\n"
        }

        text += if (transaction.isResponseBodyPlainText) {
            if (transaction.responseBody.isNullOrBlank()) {
                context.getString(R.string.chucker_body_empty)
            } else {
                transaction.getFormattedResponseBody()
            }
        } else {
            context.getString(R.string.chucker_body_omitted)
        }

        return text
    }

    fun getShareCurlCommand(transaction: HttpTransaction): String {
        var compressed = false
        var curlCmd = "curl -X ${transaction.method}"
        val headers = transaction.getParsedRequestHeaders()

        headers?.forEach { header ->
            if ("Accept-Encoding".equals(header.name, ignoreCase = true) &&
                "gzip".equals(header.value, ignoreCase = true)
            ) {
                compressed = true
            }
            curlCmd += " -H \"${header.name}: ${header.value}\""
        }

        val requestBody = transaction.requestBody
        if (!requestBody.isNullOrEmpty()) {
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data $'${requestBody.replace("\n", "\\n")}'"
        }
        curlCmd += (if (compressed) " --compressed " else " ") + transaction.getFormattedUrl(encode = false)
        return curlCmd
    }
}
