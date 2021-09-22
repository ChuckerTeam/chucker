package com.chuckerteam.chucker.internal.support

import okhttp3.Headers
import okhttp3.Response
import okio.Source
import okio.buffer
import okio.gzip
import okio.source
import org.brotli.dec.BrotliInputStream
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK
import java.util.Locale

private const val HTTP_CONTINUE = 100

/** Returns true if the response must have a (possibly 0-length) body. See RFC 7231.  */
internal fun Response.hasBody(): Boolean {
    // HEAD requests never yield a body regardless of the response headers.
    if (request.method == "HEAD") {
        return false
    }

    val responseCode = code
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

private val Response.contentLength: Long
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

private val Headers.containsGzip: Boolean
    get() {
        return this["Content-Encoding"].equals("gzip", ignoreCase = true)
    }

private val Headers.containsBrotli: Boolean
    get() {
        return this["Content-Encoding"].equals("br", ignoreCase = true)
    }

private val supportedEncodings = listOf("identity", "gzip", "br")

internal val Headers.hasSupportedContentEncoding: Boolean
    get() = get("Content-Encoding")
        ?.takeIf { it.isNotEmpty() }
        ?.let { it.lowercase(Locale.ROOT) in supportedEncodings }
        ?: true

internal fun Source.uncompress(headers: Headers) = when {
    headers.containsGzip -> gzip()
    headers.containsBrotli -> BrotliInputStream(this.buffer().inputStream()).source()
    else -> this
}

internal fun Headers.redact(names: Iterable<String>): Headers {
    val builder = newBuilder()
    for (name in names()) {
        if (names.any { userHeader -> userHeader.equals(name, ignoreCase = true) }) {
            builder[name] = "**"
        }
    }
    return builder.build()
}
