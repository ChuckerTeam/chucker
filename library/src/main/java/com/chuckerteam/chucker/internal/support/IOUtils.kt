package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import okio.Buffer
import okio.BufferedSource
import okio.GzipSource
import okio.buffer
import java.io.EOFException
import java.nio.charset.Charset
import kotlin.math.min

private const val PREFIX_SIZE = 64L
private const val CODE_POINT_SIZE = 16

internal class IOUtils(private val context: Context) {

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < PREFIX_SIZE) buffer.size else PREFIX_SIZE
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0 until CODE_POINT_SIZE) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    fun readFromBuffer(buffer: Buffer, charset: Charset, maxContentLength: Long): String {
        val bufferSize = buffer.size
        val maxBytes = min(bufferSize, maxContentLength)
        var body = ""
        try {
            body = buffer.readString(maxBytes, charset)
        } catch (e: EOFException) {
            body += context.getString(R.string.chucker_body_unexpected_eof)
        }

        if (bufferSize > maxContentLength) {
            body += context.getString(R.string.chucker_body_content_truncated)
        }
        return body
    }

    fun getNativeSource(input: BufferedSource, isGzipped: Boolean): BufferedSource = if (isGzipped) {
        val source = GzipSource(input)
        source.use { it.buffer() }
    } else {
        input
    }

    fun bodyHasSupportedEncoding(contentEncoding: String?) =
        contentEncoding.isNullOrEmpty() ||
            contentEncoding.equals("identity", ignoreCase = true) ||
            contentEncoding.equals("gzip", ignoreCase = true)
}
