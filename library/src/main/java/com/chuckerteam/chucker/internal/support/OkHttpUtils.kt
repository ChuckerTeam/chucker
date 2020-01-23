@file:JvmName("OkHttpUtils")

package com.chuckerteam.chucker.internal.support

import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK
import okhttp3.Response

const val HTTP_CONTINUE = 100

/** Returns true if the response must have a (possibly 0-length) body. See RFC 7231.  */
internal fun Response.hasBody(): Boolean {
    // HEAD requests never yield a body regardless of the response headers.
    if (this.request().method() == "HEAD") {
        return false
    }

    val responseCode = this.code()
    if ((responseCode < HTTP_CONTINUE || responseCode >= HTTP_OK) &&
        responseCode != HTTP_NO_CONTENT &&
        responseCode != HTTP_NOT_MODIFIED
    ) {
        return true
    }

    // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
    // response is malformed. For best compatibility, we honor the headers.
    return this.contentLenght != -1L || this.isChunked
}

internal val Response.contentLenght: Long
    get() {
        return this.header("Content-Length")?.toLongOrNull() ?: -1
    }

internal val Response.isChunked: Boolean
    get() {
        return this.header("Transfer-Encoding").equals("chunked", ignoreCase = true)
    }
