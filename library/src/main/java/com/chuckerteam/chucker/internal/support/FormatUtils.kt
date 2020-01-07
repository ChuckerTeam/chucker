package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale
import javax.xml.XMLConstants
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult
import kotlin.math.ln
import kotlin.math.pow
import org.xml.sax.InputSource

internal object FormatUtils {

    private const val SI_MULTIPLE = 1000
    private const val BASE_TWO_MULTIPLE = 1024

    fun formatHeaders(httpHeaders: List<HttpHeader>?, withMarkup: Boolean): String {
        return httpHeaders?.joinToString(separator = "") { header ->
            if (withMarkup) {
                "<b> ${header.name}: </b>${header.value} <br />"
            } else {
                "${header.name}: ${header.value}\n"
            }
        } ?: ""
    }

    fun formatByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) SI_MULTIPLE else BASE_TWO_MULTIPLE

        if (bytes < unit) {
            return "$bytes B"
        }

        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"

        return String.format(Locale.US, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    fun formatJson(json: String): String {
        return try {
            val je = JsonParser.parseString(json)
            JsonConverter.instance.toJson(je)
        } catch (e: JsonParseException) {
            json
        }
    }

    fun formatXml(xml: String): String {
        return try {
            val transformerFactory = SAXTransformerFactory.newInstance().apply {
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
                setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "")
            }

            val serializer = transformerFactory.newTransformer().apply {
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            }

            val xmlSource = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
            val res = StreamResult(ByteArrayOutputStream())
            serializer.transform(xmlSource, res)
            ((res.outputStream as ByteArrayOutputStream).toByteArray()).toString()
        } catch (e: TransformerException) {
            xml
        }
    }

    fun getShareText(context: Context, transaction: HttpTransaction): String {
        var text = "${context.getString(R.string.chucker_url)}: ${transaction.url}\n"
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

        var headers = formatHeaders(transaction.getParsedRequestHeaders(), false)

        if (headers.isNotBlank()) {
            text += "${headers}\n"
        }

        text += if (transaction.isRequestBodyPlainText) {
            transaction.getFormattedRequestBody()
        } else {
            context.getString(R.string.chucker_body_omitted)
        }

        text += "\n\n"
        text += "---------- ${context.getString(R.string.chucker_response)} ----------\n\n"

        headers = formatHeaders(transaction.getParsedResponseHeaders(), false)

        if (headers.isNotBlank()) {
            text += "${headers}\n"
        }

        text += if (transaction.isResponseBodyPlainText) {
            transaction.getFormattedResponseBody()
        } else {
            context.getString(R.string.chucker_body_omitted)
        }

        return text
    }

    fun getShareCurlCommand(transaction: HttpTransaction): String {
        var compressed = false
        var curlCmd = "curl -X $transaction.method"
        val headers = transaction.getParsedRequestHeaders()

        headers?.forEach { header ->
            if ("Accept-Encoding".equals(header.name, ignoreCase = true) &&
                "gzip".equals(header.value, ignoreCase = true)
            ) {
                compressed = true
            }
            curlCmd += " -H \"$header.name: $header.value\""
        }

        val requestBody = transaction.requestBody
        if (!requestBody.isNullOrEmpty()) {
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data $'$requestBody.replace(\"\\n\", \"\\\\n\")'"
        }
        curlCmd += (if (compressed) " --compressed " else " ") + transaction.url
        return curlCmd
    }

    /**
     * Convert a stacktrace into a String.
     *
     * @param throwable The throwable to convert
     * @return The String of the throwable
     */
    fun formatThrowable(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
}
