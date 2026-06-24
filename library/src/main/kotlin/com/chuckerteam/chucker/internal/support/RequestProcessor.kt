package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okhttp3.MultipartBody
import okhttp3.Request
import okio.Buffer
import okio.ByteString
import okio.IOException

internal class RequestProcessor(
    private val context: Context,
    private val collector: ChuckerCollector,
    private val maxContentLength: Long,
    private val headersToRedact: Set<String>,
    private val bodyDecoders: List<BodyDecoder>,
) {
    private companion object {
        const val MAX_PREFIX_LENGTH = 64L
        const val MAX_CODEPOINTS_TO_CHECK = 16
    }

    fun process(
        request: Request,
        transaction: HttpTransaction,
    ) {
        processMetadata(request, transaction)
        processPayload(request, transaction)
        collector.onRequestSent(transaction)
    }

    private fun processMetadata(
        request: Request,
        transaction: HttpTransaction,
    ) {
        transaction.apply {
            requestHeadersSize = request.headers.byteCount()
            request.headers.redact(headersToRedact).let {
                setRequestHeaders(it)
                setGraphQlOperationName(it)
            }
            populateUrl(request.url)
            graphQlDetected = isGraphQLRequest(this.graphQlOperationName, request)

            requestDate = System.currentTimeMillis()
            method = request.method
            requestContentType = request.body?.contentType()?.toString()
            requestPayloadSize = request.body?.contentLength()
        }
    }

    private fun processPayload(
        request: Request,
        transaction: HttpTransaction,
    ) {
        val body = request.body ?: return
        if (body.isOneShot()) {
            Logger.info("Skipping one shot request body")
            return
        }
        if (body.isDuplex()) {
            Logger.info("Skipping duplex request body")
            return
        }

        if (body is MultipartBody) {
            val content = processMultipartPayload(body)
            transaction.requestBody = content
            transaction.isRequestBodyEncoded = false
            return
        }

        val requestSource =
            try {
                Buffer().apply { body.writeTo(this) }
            } catch (e: IOException) {
                Logger.error("Failed to read request payload", e)
                return
            }
        val limitingSource =
            LimitingSource(requestSource.uncompress(request.headers), maxContentLength)

        val contentBuffer = Buffer().apply { limitingSource.use { writeAll(it) } }

        val decodedContent = decodePayload(request, contentBuffer.readByteString())
        transaction.requestBody = decodedContent
        transaction.isRequestBodyEncoded = decodedContent == null
        if (decodedContent != null && limitingSource.isThresholdReached) {
            transaction.requestBody += context.getString(R.string.chucker_body_content_truncated)
        }
    }

    private fun processMultipartPayload(body: MultipartBody): String {
        return buildString {
            body.parts.forEach { part ->
                part.headers?.forEach { header ->
                    append(header.first + ": " + header.second + "\n")
                }
                val partBody = part.body
                if (partBody.contentType() != null) {
                    append("Content-Type: ${partBody.contentType()}\n")
                }
                if (partBody.contentLength() != -1L) {
                    append("Content-Length: ${partBody.contentLength()}\n")
                }

                val buffer = Buffer()
                partBody.writeTo(buffer)

                if (isPlainText(buffer)) {
                    append("\n")
                    append(buffer.readUtf8())
                } else {
                    append("\n(binary: ${partBody.contentLength()} bytes)")
                }
                append("\n\n")

                if (length >= maxContentLength) {
                    append(context.getString(R.string.chucker_body_content_truncated))
                    return@buildString
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun isPlainText(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < MAX_PREFIX_LENGTH) buffer.size else MAX_PREFIX_LENGTH
            buffer.copyTo(prefix, 0, byteCount)
            repeat(MAX_CODEPOINTS_TO_CHECK) {
                if (prefix.exhausted()) {
                    return@repeat
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun decodePayload(
        request: Request,
        body: ByteString,
    ) = bodyDecoders
        .asSequence()
        .mapNotNull { decoder ->
            try {
                Logger.info("Decoding with: $decoder")
                decoder.decodeRequest(request, body)
            } catch (e: IOException) {
                Logger.warn("Decoder $decoder failed to process request payload", e)
                null
            }
        }.firstOrNull()

    private fun isGraphQLRequest(
        graphQLOperationName: String?,
        request: Request,
    ) = graphQLOperationName != null ||
        request.url.pathSegments.contains("graphql") ||
        request.url.host.contains("graphql")
}
