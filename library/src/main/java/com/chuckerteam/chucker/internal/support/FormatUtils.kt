package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale
import javax.xml.XMLConstants
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactoryConfigurationError
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

private const val THOUSAND = 1000
private const val ONE_K = 1024

fun Long?.formatBytes(): String = when {
    this == null -> ""
    else -> formatByteCount(this, true)
}

fun Int?.formatBytes(): String = when {
    this == null -> ""
    else -> formatByteCount(this.toLong(), true)
}

fun formatByteCount(bytes: Long, si: Boolean): String {
    val unit = if (si) THOUSAND else ONE_K
    if (bytes < unit) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format(
        Locale.US,
        "%.1f %sB",
        bytes / Math.pow(unit.toDouble(), exp.toDouble()),
        pre
    )
}

internal fun formatHeaders(httpHeaders: List<HttpHeader>?, withMarkup: Boolean): String {
    var out = ""
    if (httpHeaders != null) {
        for ((name, value) in httpHeaders) {
            out += (if (withMarkup) "<b>" else "") + name + ": " + (if (withMarkup) "</b>" else "") +
                value + if (withMarkup) "<br />" else "\n"
        }
    }
    return out
}

fun formatJson(json: String): String = try {
    val jp = JsonParser()
    val je = jp.parse(json)
    JsonConverter.instance.toJson(je)
} catch (e: JsonIOException) {
    json
}

fun formatXml(xml: String): String {
    try {
        val transformerFactory = SAXTransformerFactory.newInstance() as SAXTransformerFactory
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        transformerFactory.setAttribute(
            "http://javax.xml.XMLConstants/property/accessExternalDTD",
            ""
        )
        transformerFactory.setAttribute(
            "http://javax.xml.XMLConstants/property/accessExternalStylesheet",
            ""
        )
        val serializer = transformerFactory.newTransformer()
        serializer.setOutputProperty(OutputKeys.INDENT, "yes")
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val xmlSource = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
        val res = StreamResult(ByteArrayOutputStream())
        serializer.transform(xmlSource, res)

        return String((res.outputStream as ByteArrayOutputStream).toByteArray())
    } catch (e: TransformerFactoryConfigurationError) {
        return xml
    } catch (e: TransformerConfigurationException) {
        return xml
    } catch (e: TransformerException) {
        return xml
    }
}

internal fun getShareText(context: Context, transaction: HttpTransaction): String {
    val isSsl = context.getString(
        if (transaction.isSsl) R.string.chucker_yes else R.string.chucker_no
    )
    val requestBody = if (transaction.isRequestBodyPlainText) {
        transaction.getFormattedRequestBody()
    } else {
        context.getString(R.string.chucker_body_omitted)
    }
    val responseBody = if (transaction.isResponseBodyPlainText) {
        transaction.getFormattedResponseBody()
    } else {
        context.getString(R.string.chucker_body_omitted)
    }
    return """
    ${context.getString(R.string.chucker_url)}: ${transaction.url ?: ""}
    ${context.getString(R.string.chucker_method)}: ${transaction.method ?: ""}
    ${context.getString(R.string.chucker_protocol)}: ${transaction.protocol ?: ""}
    ${context.getString(R.string.chucker_status)}: ${transaction.status}
    ${context.getString(R.string.chucker_response)}: ${transaction.responseSummaryText ?: ""}
    ${context.getString(R.string.chucker_ssl)}: ${isSsl ?: ""}
    
    ${context.getString(R.string.chucker_request_time)}: ${transaction.requestDateString ?: ""}
    ${context.getString(R.string.chucker_response_time)}: ${transaction.responseDateString ?: ""}
    ${context.getString(R.string.chucker_duration)}: ${transaction.durationString ?: ""}
    
    ${context.getString(R.string.chucker_request_size)}: ${transaction.requestSizeString}
    ${context.getString(R.string.chucker_response_size)}: ${transaction.responseSizeString ?: ""}
    ${context.getString(R.string.chucker_total_size)}: ${transaction.totalSizeString}
    
    ---------- ${context.getString(R.string.chucker_request)} ----------

    ${formatHeaders(transaction.getParsedRequestHeaders(), false)}
    $requestBody
    
    ---------- ${context.getString(R.string.chucker_response)} ----------
    
    ${formatHeaders(transaction.getParsedResponseHeaders(), false)}
    $responseBody
    """.trimIndent()
}

internal fun getShareCurlCommand(transaction: HttpTransaction): String {
    var compressed = false
    var curlCmd = "curl"
    curlCmd += " -X " + (transaction.method ?: "")

    transaction.getParsedRequestHeaders()?.let { headers ->
        var i = 0
        val count = headers.size
        while (i < count) {
            val name = headers[i].name
            val value = headers[i].value
            if ("Accept-Encoding".equals(name, ignoreCase = true) &&
                "gzip".equals(value, ignoreCase = true)
            ) {
                compressed = true
            }
            curlCmd += " -H \"$name: $value\""
            i++
        }
    }

    val requestBody = transaction.requestBody
    if (!requestBody.isNullOrEmpty()) {
        // try to keep to a single line and use a subshell to preserve any line breaks
        curlCmd += " --data $'" + requestBody.replace("\n", "\\n") + "'"
    }
    curlCmd += (if (compressed) " --compressed " else " ") + (transaction.url ?: "")
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
