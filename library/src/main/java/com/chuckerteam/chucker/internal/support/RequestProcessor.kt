package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okhttp3.Request
import okio.Buffer
import okio.ForwardingSource
import okio.IOException
import okio.Source
import kotlin.text.Charsets.UTF_8

internal class RequestProcessor(
    private val context: Context,
    private val collector: ChuckerCollector,
    private val maxContentLength: Long,
) {
    fun process(request: Request, transaction: HttpTransaction): Request {
        processMetadata(request, transaction)
        processBody(request, transaction)
        collector.onRequestSent(transaction)
        return request
    }

    private fun processMetadata(request: Request, transaction: HttpTransaction) {
        transaction.apply {
            setRequestHeaders(request.headers)
            populateUrl(request.url)

            requestDate = System.currentTimeMillis()
            method = request.method
            requestContentType = request.body?.contentType()?.toString()
            requestPayloadSize = request.body?.contentLength()
        }
    }

    private fun processBody(request: Request, transaction: HttpTransaction) {
        val body = request.body ?: return
        if (body.isOneShot()) {
            Logger.info("Skipping one shot request body")
            return
        }
        if (body.isDuplex()) {
            Logger.info("Skipping duplex request body")
            return
        }

        val isEncodingSupported = request.headers.hasSupportedContentEncoding
        if (!isEncodingSupported) {
            return
        }

        val limitingSource = try {
            Buffer().apply { body.writeTo(this) }
        } catch (e: IOException) {
            Logger.error("Failed to read request payload", e)
            return
        }.uncompress(request.headers).let { LimitingSource(it, maxContentLength) }

        val contentBuffer = Buffer().apply { limitingSource.use { writeAll(it) } }
        if (!contentBuffer.isProbablyPlainText) {
            return
        }

        transaction.isRequestBodyPlainText = true
        try {
            transaction.requestBody = contentBuffer.readString(body.contentType()?.charset() ?: UTF_8)
        } catch (e: IOException) {
            Logger.error("Failed to process request payload", e)
        }
        if (limitingSource.isLimitReached) {
            transaction.requestBody += context.getString(R.string.chucker_body_content_truncated)
        }
    }

    private class LimitingSource(
        delegate: Source,
        private val bytesReadLimit: Long,
    ) : ForwardingSource(delegate) {
        private var bytesRead = 0L
        val isLimitReached get() = bytesRead >= bytesReadLimit

        override fun read(sink: Buffer, byteCount: Long) = if (!isLimitReached) {
            super.read(sink, byteCount).also { bytesRead += it }
        } else {
            -1L
        }
    }
}
