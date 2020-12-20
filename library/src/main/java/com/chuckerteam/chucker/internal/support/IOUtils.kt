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

internal class IOUtils(private val context: Context) {

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
