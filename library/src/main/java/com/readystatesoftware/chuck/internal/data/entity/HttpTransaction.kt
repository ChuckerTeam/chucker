package com.readystatesoftware.chuck.internal.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.net.Uri
import com.google.gson.reflect.TypeToken
import com.readystatesoftware.chuck.internal.support.FormatUtils
import com.readystatesoftware.chuck.internal.support.JsonConvertor
import okhttp3.Headers
import java.util.ArrayList

/**
 * Represent a full HTTP transaction (with Request and Response). Instances of this classes
 * should be populated as soon as the library receives data from OkHttp.
 */
@Entity(tableName = "transactions")
internal class HttpTransaction(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
        @ColumnInfo(name = "requestDate") var requestDate: Long?,
        @ColumnInfo(name = "responseDate") var responseDate: Long?,
        @ColumnInfo(name = "tookMs") var tookMs: Long?,
        @ColumnInfo(name = "protocol") var protocol: String?,
        @ColumnInfo(name = "method") var method: String?,
        @ColumnInfo(name = "url") var url: String?,
        @ColumnInfo(name = "host") var host: String?,
        @ColumnInfo(name = "path") var path: String?,
        @ColumnInfo(name = "scheme") var scheme: String?,
        @ColumnInfo(name = "requestContentLength") var requestContentLength: Long?,
        @ColumnInfo(name = "requestContentType") var requestContentType: String?,
        @ColumnInfo(name = "requestHeaders") var requestHeaders: String?,
        @ColumnInfo(name = "requestBody") var requestBody: String?,
        @ColumnInfo(name = "isRequestBodyPlainText") var isRequestBodyPlainText: Boolean = true,
        @ColumnInfo(name = "responseCode") var responseCode: Int?,
        @ColumnInfo(name = "responseMessage") var responseMessage: String?,
        @ColumnInfo(name = "error") var error: String?,
        @ColumnInfo(name = "responseContentLength") var responseContentLength: Long?,
        @ColumnInfo(name = "responseContentType") var responseContentType: String?,
        @ColumnInfo(name = "responseHeaders") var responseHeaders: String?,
        @ColumnInfo(name = "responseBody") var responseBody: String?,
        @ColumnInfo(name = "isResponseBodyPlainText") var isResponseBodyPlainText: Boolean = true

) {

    @Ignore
    constructor() : this(
            requestDate = null,
            responseDate = null,
            tookMs = null,
            protocol = null,
            method = null,
            url = null,
            host = null,
            path = null,
            scheme = null,
            requestContentLength = null,
            requestContentType = null,
            requestHeaders = null,
            requestBody = null,
            responseCode = null,
            responseMessage = null,
            error = null,
            responseContentLength = null,
            responseContentType = null,
            responseHeaders = null,
            responseBody = null)

    enum class Status {
        Requested,
        Complete,
        Failed
    }

    val status: Status
        get() = when {
            error != null -> Status.Failed
            responseCode == null -> Status.Requested
            else -> Status.Complete
        }

    val requestDateString: String?
        get() = requestDate?.toString()

    val responseDateString: String?
        get() = responseDate?.toString()

    val durationString: String?
        get() = tookMs?.let { "$it ms" }

    val requestSizeString: String
        get() = formatBytes(requestContentLength ?: 0)

    val responseSizeString: String?
        get() = responseContentLength?.let { formatBytes(it) }

    val totalSizeString: String
        get() {
            val reqBytes = requestContentLength ?: 0
            val resBytes = responseContentLength ?: 0
            return formatBytes(reqBytes + resBytes)
        }

    val responseSummaryText: String?
        get() {
            return when (status) {
                Status.Failed -> error
                Status.Requested -> null
                else -> responseCode.toString() + " " + responseMessage
            }
        }

    val notificationText: String
        get() {
            return when (status) {
                Status.Failed -> " ! ! !  $method $path"
                Status.Requested -> " . . .  $method $path"
                else -> responseCode.toString() + " " + method + " " + path
            }
        }

    val isSsl: Boolean
        get() = scheme?.toLowerCase() == "https"


    fun setRequestHeaders(headers: Headers) {
        setRequestHeaders(toHttpHeaderList(headers))
    }

    fun setRequestHeaders(headers: List<HttpHeader>) {
        requestHeaders = JsonConvertor.getInstance().toJson(headers)
    }

    fun getParsedRequestHeaders(): List<HttpHeader>? {
        return JsonConvertor.getInstance().fromJson<List<HttpHeader>>(requestHeaders,
                object : TypeToken<List<HttpHeader>>() {

                }.type)
    }

    fun getParsedResponseHeaders(): List<HttpHeader>? {
        return JsonConvertor.getInstance().fromJson<List<HttpHeader>>(responseHeaders,
                object : TypeToken<List<HttpHeader>>() {
                }.type)
    }

    fun getRequestHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedRequestHeaders(), withMarkup)
    }

    fun setResponseHeaders(headers: Headers) {
        setResponseHeaders(toHttpHeaderList(headers))
    }

    fun setResponseHeaders(headers: List<HttpHeader>) {
        responseHeaders = JsonConvertor.getInstance().toJson(headers)
    }

    fun getResponseHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedResponseHeaders(), withMarkup)
    }

    private fun toHttpHeaderList(headers: Headers): List<HttpHeader> {
        val httpHeaders = ArrayList<HttpHeader>()
        var i = 0
        val count = headers.size()
        while (i < count) {
            httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
            i++
        }
        return httpHeaders
    }

    private fun formatBody(body: String, contentType: String?): String {
        return if (contentType != null && contentType.toLowerCase().contains("json")) {
            FormatUtils.formatJson(body)
        } else if (contentType != null && contentType.toLowerCase().contains("xml")) {
            FormatUtils.formatXml(body)
        } else {
            body
        }
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    fun getFormattedRequestBody(): String {
        return requestBody?.let { formatBody(it, requestContentType) } ?: ""
    }

    fun getFormattedResponseBody(): String {
        return responseBody?.let { formatBody(it, requestContentType) } ?: ""
    }

    fun populateUrl(url: String): HttpTransaction {
        this.url = url
        val uri = Uri.parse(url)
        host = uri.host
        path = ("${uri.path}${if (uri.query != null) "?" else ""}${uri.query}").toString()
        scheme = uri.scheme
        return this
    }

}
