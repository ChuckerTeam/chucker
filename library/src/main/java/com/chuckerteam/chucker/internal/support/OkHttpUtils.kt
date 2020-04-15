package com.chuckerteam.chucker.internal.support

import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response

private const val HTTP_CONTINUE = 100

/** Returns true if the response must have a (possibly 0-length) body. See RFC 7231.  */
internal fun Response.hasBody(): Boolean {
    // HEAD requests never yield a body regardless of the response headers.
    if (request().method() == "HEAD") {
        return false
    }

    val responseCode = code()
    if ((responseCode < HTTP_CONTINUE || responseCode >= HTTP_OK) &&
        (responseCode != HTTP_NO_CONTENT) &&
        (responseCode != HTTP_NOT_MODIFIED)
    ) {
        return true
    }

    // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
    // response is malformed. For best compatibility, we honor the headers.
    return ((contentLength > 0) || isChunked)
}

internal val Response.contentLength: Long
    get() {
        return this.header("Content-Length")?.toLongOrNull() ?: -1L
    }

internal val Response.isChunked: Boolean
    get() {
        return this.header("Transfer-Encoding").equals("chunked", ignoreCase = true)
    }

internal val Response.contentType: String?
    get() {
        return this.header("Content-Type")
    }

/** Checks if the OkHttp response uses gzip encoding. */
internal val Response.isGzipped: Boolean
    get() {
        return this.headers().containsGzip
    }

/** Checks if the OkHttp request uses gzip encoding. */
internal val Request.isGzipped: Boolean
    get() {
        return this.headers().containsGzip
    }

private val Headers.containsGzip: Boolean
    get() {
        return this["Content-Encoding"].equals("gzip", ignoreCase = true)
    }
