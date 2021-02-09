package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.Source
import okio.buffer
import okio.source
import java.io.File

internal class ResponseProcessor(
    private val collector: ChuckerCollector,
    private val cacheDirectoryProvider: CacheDirectoryProvider,
    private val maxContentLength: Long,
    private val headersToRedact: Set<String>,
    private val alwaysReadResponseBody: Boolean,
) {
    fun process(response: Response, transaction: HttpTransaction): Response {
        processResponseMetadata(response, transaction)
        return multiCastResponse(response, transaction)
    }

    private fun processResponseMetadata(response: Response, transaction: HttpTransaction) {
        val responseEncodingIsSupported = response.headers.hasSupportedContentEncoding

        transaction.apply {
            // includes headers added later in the chain
            setRequestHeaders(response.request.headers.redact(headersToRedact))
            setResponseHeaders(response.headers.redact(headersToRedact))

            isResponseBodyPlainText = responseEncodingIsSupported
            requestDate = response.sentRequestAtMillis
            responseDate = response.receivedResponseAtMillis
            protocol = response.protocol.toString()
            responseCode = response.code
            responseMessage = response.message

            response.handshake?.let { handshake ->
                responseTlsVersion = handshake.tlsVersion.javaName
                responseCipherSuite = handshake.cipherSuite.javaName
            }

            responseContentType = response.contentType

            tookMs = (response.receivedResponseAtMillis - response.sentRequestAtMillis)
        }
    }

    private fun multiCastResponse(response: Response, transaction: HttpTransaction): Response {
        val responseBody = response.body
        if (!response.hasBody() || responseBody == null) {
            collector.onResponseReceived(transaction)
            return response
        }

        val contentType = responseBody.contentType()
        val contentLength = responseBody.contentLength()

        val sideStream = ReportingSink(
            createTempTransactionFile(),
            ResponseReportingSinkCallback(response, transaction),
            maxContentLength
        )
        var upstream: Source = TeeSource(responseBody.source(), sideStream)
        if (alwaysReadResponseBody) upstream = DepletingSource(upstream)

        return response.newBuilder()
            .body(upstream.buffer().asResponseBody(contentType, contentLength))
            .build()
    }

    private fun createTempTransactionFile(): File? {
        val cache = cacheDirectoryProvider.provide()
        return if (cache == null) {
            Logger.warn("Failed to obtain a valid cache directory for transaction files")
            null
        } else {
            FileFactory.create(cache)
        }
    }

    private fun processResponsePayload(response: Response, payload: Buffer, transaction: HttpTransaction) {
        val responseBody = response.body ?: return

        val contentType = responseBody.contentType()
        val charset = contentType?.charset() ?: Charsets.UTF_8

        if (payload.isProbablyPlainText) {
            transaction.isResponseBodyPlainText = true
            if (payload.size != 0L) {
                transaction.responseBody = payload.readString(charset)
            }
        } else {
            transaction.isResponseBodyPlainText = false

            val isImageContentType = contentType?.toString()?.contains(CONTENT_TYPE_IMAGE, ignoreCase = true) == true

            if (isImageContentType && (payload.size < MAX_BLOB_SIZE)) {
                transaction.responseImageData = payload.readByteArray()
            }
        }
    }

    private inner class ResponseReportingSinkCallback(
        private val response: Response,
        private val transaction: HttpTransaction,
    ) : ReportingSink.Callback {

        override fun onClosed(file: File?, sourceByteCount: Long) {
            file?.readResponsePayload()?.let { payload ->
                processResponsePayload(response, payload, transaction)
            }
            transaction.responsePayloadSize = sourceByteCount
            collector.onResponseReceived(transaction)
            file?.delete()
        }

        override fun onFailure(file: File?, exception: java.io.IOException) {
            Logger.error("Failed to read response payload", exception)
        }

        private fun File.readResponsePayload() = try {
            source().uncompress(response.headers).use { source ->
                Buffer().apply { writeAll(source) }
            }
        } catch (e: java.io.IOException) {
            Logger.error("Response payload couldn't be processed", e)
            null
        }
    }

    private companion object {
        const val MAX_BLOB_SIZE = 1_000_000L

        const val CONTENT_TYPE_IMAGE = "image"
    }
}
