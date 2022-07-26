@file:Suppress("TooManyFunctions")

package com.chuckerteam.chucker.internal.data.entity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.support.FormattedUrl
import com.chuckerteam.chucker.internal.support.JsonConverter
import com.google.gson.reflect.TypeToken
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.HttpURLConnection
import java.util.Date

/**
 * Represent a full HTTP transaction (with Request and Response). Instances of this classes
 * should be populated as soon as the library receives data from OkHttp.
 */
@Suppress("LongParameterList")
@Entity(tableName = "transactions")
internal class HttpTransaction(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    var id: Long = 0,
    @ColumnInfo(name = "requestDate") var requestDate: Long?,
    @ColumnInfo(name = "responseDate") var responseDate: Long?,
    @ColumnInfo(name = "tookMs") var tookMs: Long?,
    @ColumnInfo(name = "protocol") var protocol: String?,
    @ColumnInfo(name = "method") var method: String?,
    @ColumnInfo(name = "url") var url: String?,
    @ColumnInfo(name = "host") var host: String?,
    @ColumnInfo(name = "path") var path: String?,
    @ColumnInfo(name = "scheme") var scheme: String?,
    @ColumnInfo(name = "responseTlsVersion") var responseTlsVersion: String?,
    @ColumnInfo(name = "responseCipherSuite") var responseCipherSuite: String?,
    @ColumnInfo(name = "requestPayloadSize") var requestPayloadSize: Long?,
    @ColumnInfo(name = "requestContentType") var requestContentType: String?,
    @ColumnInfo(name = "requestHeaders") var requestHeaders: String?,
    @ColumnInfo(name = "requestHeadersSize") var requestHeadersSize: Long?,
    @ColumnInfo(name = "requestBody") var requestBody: String?,
    @ColumnInfo(name = "isRequestBodyEncoded") var isRequestBodyEncoded: Boolean = false,
    @ColumnInfo(name = "responseCode") var responseCode: Int?,
    @ColumnInfo(name = "responseMessage") var responseMessage: String?,
    @ColumnInfo(name = "error") var error: String?,
    @ColumnInfo(name = "responsePayloadSize") var responsePayloadSize: Long?,
    @ColumnInfo(name = "responseContentType") var responseContentType: String?,
    @ColumnInfo(name = "responseHeaders") var responseHeaders: String?,
    @ColumnInfo(name = "responseHeadersSize") var responseHeadersSize: Long?,
    @ColumnInfo(name = "responseBody") var responseBody: String?,
    @ColumnInfo(name = "isResponseBodyEncoded") var isResponseBodyEncoded: Boolean = false,
    @ColumnInfo(name = "responseImageData") var responseImageData: ByteArray?
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
        responseTlsVersion = null,
        responseCipherSuite = null,
        requestPayloadSize = null,
        requestContentType = null,
        requestHeaders = null,
        requestHeadersSize = null,
        requestBody = null,
        responseCode = null,
        responseMessage = null,
        error = null,
        responsePayloadSize = null,
        responseContentType = null,
        responseHeaders = null,
        responseHeadersSize = null,
        responseBody = null,
        responseImageData = null
    )

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
        get() = requestDate?.let { Date(it).toString() }

    val responseDateString: String?
        get() = responseDate?.let { Date(it).toString() }

    val durationString: String?
        get() = tookMs?.let { "$it ms" }

    val requestSizeString: String
        get() = formatBytes(requestPayloadSize ?: 0)

    val responseSizeString: String?
        get() = responsePayloadSize?.let { formatBytes(it) }

    val totalSizeString: String
        get() {
            val reqBytes = requestPayloadSize ?: 0
            val resBytes = responsePayloadSize ?: 0
            return formatBytes(reqBytes + resBytes)
        }

    val responseSummaryText: String?
        get() {
            return when (status) {
                Status.Failed -> error
                Status.Requested -> null
                else -> "$responseCode $responseMessage"
            }
        }

    val notificationText: String
        get() {
            return when (status) {
                Status.Failed -> " ! ! !  $method $path"
                Status.Requested -> " . . .  $method $path"
                else -> "$responseCode $method $path"
            }
        }

    val isSsl: Boolean
        get() = scheme.equals("https", ignoreCase = true)

    val responseImageBitmap: Bitmap?
        get() {
            return responseImageData?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        }

    fun setRequestHeaders(headers: Headers) {
        setRequestHeaders(toHttpHeaderList(headers))
    }

    fun setRequestHeaders(headers: List<HttpHeader>) {
        requestHeaders = JsonConverter.instance.toJson(headers)
    }

    fun getParsedRequestHeaders(): List<HttpHeader>? {
        return JsonConverter.instance.fromJson<List<HttpHeader>>(
            requestHeaders,
            object : TypeToken<List<HttpHeader>>() {
            }.type
        )
    }

    fun getParsedResponseHeaders(): List<HttpHeader>? {
        return JsonConverter.instance.fromJson<List<HttpHeader>>(
            responseHeaders,
            object : TypeToken<List<HttpHeader>>() {
            }.type
        )
    }

    fun getRequestHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedRequestHeaders(), withMarkup)
    }

    fun setResponseHeaders(headers: Headers) {
        setResponseHeaders(toHttpHeaderList(headers))
    }

    fun setResponseHeaders(headers: List<HttpHeader>) {
        responseHeaders = JsonConverter.instance.toJson(headers)
    }

    fun getResponseHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getParsedResponseHeaders(), withMarkup)
    }

    private fun toHttpHeaderList(headers: Headers): List<HttpHeader> {
        val httpHeaders = ArrayList<HttpHeader>()
        for (i in 0 until headers.size) {
            httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
        }
        return httpHeaders
    }

    private fun formatBody(body: String, contentType: String?): String {
        return when {
            contentType.isNullOrBlank() -> body
            contentType.contains("json", ignoreCase = true) -> FormatUtils.formatJson(body)
            contentType.contains("xml", ignoreCase = true) -> FormatUtils.formatXml(body)
            contentType.contains("x-www-form-urlencoded", ignoreCase = true) ->
                FormatUtils.formatUrlEncodedForm(body)
            else -> body
        }
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    fun getFormattedRequestBody(): String {
        return requestBody?.let { formatBody(it, requestContentType) } ?: ""
    }

    fun getFormattedResponseBody(): String {
        return responseBody?.let { formatBody(it, responseContentType) } ?: ""
    }

    fun populateUrl(httpUrl: HttpUrl): HttpTransaction {
        val formattedUrl = FormattedUrl.fromHttpUrl(httpUrl, encoded = false)
        url = formattedUrl.url
        host = formattedUrl.host
        path = formattedUrl.pathWithQuery
        scheme = formattedUrl.scheme
        return this
    }

    fun getFormattedUrl(encode: Boolean): String {
        val httpUrl = url?.toHttpUrl() ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).url
    }

    fun getFormattedPath(encode: Boolean): String {
        val httpUrl = url?.toHttpUrl() ?: return ""
        return FormattedUrl.fromHttpUrl(httpUrl, encode).pathWithQuery
    }

    fun getRequestTotalSize(): Long {
        return (requestHeadersSize ?: 0) + (requestPayloadSize ?: 0)
    }

    fun getResponseTotalSize(): Long {
        return (responseHeadersSize ?: 0) + getHarResponseBodySize()
    }

    fun getHarResponseBodySize(): Long {
        return if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) 0
        else responsePayloadSize ?: 0
    }

    // Not relying on 'equals' because comparison be long due to request and response sizes
    // and it would be unwise to do this every time 'equals' is called.
    @Suppress("ComplexMethod")
    fun hasTheSameContent(other: HttpTransaction?): Boolean {
        if (this === other) return true
        if (other == null) return false

        return (id == other.id) &&
            (requestDate == other.requestDate) &&
            (responseDate == other.responseDate) &&
            (tookMs == other.tookMs) &&
            (protocol == other.protocol) &&
            (method == other.method) &&
            (url == other.url) &&
            (host == other.host) &&
            (path == other.path) &&
            (scheme == other.scheme) &&
            (responseTlsVersion == other.responseTlsVersion) &&
            (responseCipherSuite == other.responseCipherSuite) &&
            (requestPayloadSize == other.requestPayloadSize) &&
            (requestContentType == other.requestContentType) &&
            (requestHeaders == other.requestHeaders) &&
            (requestHeadersSize == other.requestHeadersSize) &&
            (requestBody == other.requestBody) &&
            (isRequestBodyEncoded == other.isRequestBodyEncoded) &&
            (responseCode == other.responseCode) &&
            (responseMessage == other.responseMessage) &&
            (error == other.error) &&
            (responsePayloadSize == other.responsePayloadSize) &&
            (responseContentType == other.responseContentType) &&
            (responseHeaders == other.responseHeaders) &&
            (responseHeadersSize == other.responseHeadersSize) &&
            (responseBody == other.responseBody) &&
            (isResponseBodyEncoded == other.isResponseBodyEncoded) &&
            (responseImageData?.contentEquals(other.responseImageData ?: byteArrayOf()) != false)
    }
}
