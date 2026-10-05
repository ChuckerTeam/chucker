package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okio.Buffer
import okio.Source

internal class TransactionCurlCommandSharable(
    private val transaction: HttpTransaction,
) : Sharable {
    override fun toSharableContent(context: Context): Source =
        Buffer().apply {
            var compressed = false
            writeUtf8("curl -X ${transaction.method}")
            val headers = transaction.getParsedRequestHeaders()

            headers?.forEach { header ->
                if (isCompressed(header)) {
                    compressed = true
                }
                val headerValue = escapeHeaderValue(header.value)
                writeUtf8(" -H \"${header.name}: ${headerValue}\"")
            }

            val requestBody = transaction.requestBody
            if (!requestBody.isNullOrEmpty()) {
                writeUtf8(" --data $'${escapeRequestBody(requestBody)}'")
            }
            writeUtf8((if (compressed) " --compressed " else " ") + transaction.getFormattedUrl(encode = true))
        }

    private fun isCompressed(header: HttpHeader): Boolean =
        (
            "Accept-Encoding".equals(header.name, ignoreCase = true) &&
                "gzip".contains(header.value, ignoreCase = true) ||
                "br".contains(header.value, ignoreCase = true)
        )

    private fun escapeHeaderValue(value: String): String {
        // escape characters that keep their special meaning inside double quotes
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .replace("`", "\\`")
    }

    private fun escapeRequestBody(body: String): String {
        // escape characters that have a special meaning inside $'...' quoting
        return body
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
    }
}
