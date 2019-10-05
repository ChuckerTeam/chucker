@file:Suppress("TooManyFunctions")

package com.chuckerteam.chucker.internal.data.entity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chuckerteam.chucker.internal.support.*
import com.chuckerteam.chucker.internal.support.formatHeaders
import com.google.gson.reflect.TypeToken
import okhttp3.Headers

/**
 * Represent a full HTTP transaction (with Request and Response). Instances of this classes
 * should be populated as soon as the library receives data from OkHttp.
 */
@Entity(tableName = "transactions")
internal class HttpTransaction(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "requestDate") var requestDate: Long? = null,
    @ColumnInfo(name = "responseDate") var responseDate: Long? = null,
    @ColumnInfo(name = "tookMs") var tookMs: Long? = null,
    @ColumnInfo(name = "protocol") var protocol: String? = null,
    @ColumnInfo(name = "method") var method: String? = null,
    @ColumnInfo(name = "url") var url: String? = null,
    @ColumnInfo(name = "host") var host: String? = null,
    @ColumnInfo(name = "path") var path: String? = null,
    @ColumnInfo(name = "scheme") var scheme: String? = null,
    @ColumnInfo(name = "requestContentLength") var requestContentLength: Long? = null,
    @ColumnInfo(name = "requestContentType") var requestContentType: String? = null,
    @ColumnInfo(name = "requestHeaders") var requestHeaders: String? = null,
    @ColumnInfo(name = "requestBody") var requestBody: String? = null,
    @ColumnInfo(name = "isRequestBodyPlainText") var isRequestBodyPlainText: Boolean = true,
    @ColumnInfo(name = "responseCode") var responseCode: Int? = null,
    @ColumnInfo(name = "responseMessage") var responseMessage: String? = null,
    @ColumnInfo(name = "error") var error: String? = null,
    @ColumnInfo(name = "responseContentLength") var responseContentLength: Long? = null,
    @ColumnInfo(name = "responseContentType") var responseContentType: String? = null,
    @ColumnInfo(name = "responseHeaders") var responseHeaders: String? = null,
    @ColumnInfo(name = "responseBody") var responseBody: String? = null,
    @ColumnInfo(name = "isResponseBodyPlainText") var isResponseBodyPlainText: Boolean = true,
    @ColumnInfo(name = "responseImageData") var responseImageData: ByteArray? = null
) : NotificationTextProducer {

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
        get() = (requestContentLength ?: 0).formatBytes()

    val responseSizeString: String?
        get() = responseContentLength?.formatBytes()

    val totalSizeString: String
        get() {
            val reqBytes = requestContentLength ?: 0
            val resBytes = responseContentLength ?: 0
            return (reqBytes + resBytes).formatBytes()
        }

    val responseSummaryText: String?
        get() {
            return when (status) {
                Status.Failed -> error
                Status.Requested -> null
                else -> "${responseCode.toString()} $responseMessage"
            }
        }

    val isSsl: Boolean
        get() = scheme?.toLowerCase() == "https"

    val responseImageBitmap: Bitmap?
        get() {
            return responseImageData?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        }

    override fun notificationId() = id

    override fun notificationText(context: Context) = when (status) {
        Status.Failed -> " ! ! !  $method $path"
        Status.Requested -> " . . .  $method $path"
        else -> "${responseCode.toString()} $method $path"
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
        return formatHeaders(getParsedRequestHeaders(), withMarkup)
    }

    fun setResponseHeaders(headers: Headers) {
        setResponseHeaders(toHttpHeaderList(headers))
    }

    fun setResponseHeaders(headers: List<HttpHeader>) {
        responseHeaders = JsonConverter.instance.toJson(headers)
    }

    fun getResponseHeadersString(withMarkup: Boolean): String {
        return formatHeaders(getParsedResponseHeaders(), withMarkup)
    }

    private fun toHttpHeaderList(headers: Headers): List<HttpHeader> =
        mutableListOf<HttpHeader>().apply {
            for (i in 0 until headers.size()) {
                add(HttpHeader(headers.name(i), headers.value(i)))
            }
        }

    private fun formatBody(body: String, contentType: String?): String = when {
        contentType != null && contentType.toLowerCase().contains("json") ->
            formatJson(body)
        contentType != null && contentType.toLowerCase().contains("xml") ->
            formatXml(body)
        else -> body
    }

    fun getFormattedRequestBody(): String {
        return requestBody?.let { formatBody(it, requestContentType) } ?: ""
    }

    fun getFormattedResponseBody(): String {
        return responseBody?.let { formatBody(it, responseContentType) } ?: ""
    }

    fun populateUrl(url: String): HttpTransaction {
        this.url = url
        val uri = Uri.parse(url)
        host = uri.host
        path = ("${uri.path}${uri.query?.let { "?$it" } ?: ""}")
        scheme = uri.scheme
        return this
    }
}
