package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.BodyDecoder
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import kotlin.text.Charsets.UTF_8

internal object PlainTextDecoder : BodyDecoder {
    override fun decodeRequest(
        request: Request,
        body: ByteString,
    ) = body.tryDecodeAsPlainText(request.headers, request.body?.contentType())

    override fun decodeResponse(
        response: Response,
        body: ByteString,
    ) = body.tryDecodeAsPlainText(response.headers, response.body?.contentType())

    private fun ByteString.tryDecodeAsPlainText(
        headers: Headers,
        contentType: MediaType?,
    ) = if (headers.hasSupportedContentEncoding && isProbablyPlainText) {
        string(contentType?.charset() ?: UTF_8)
    } else {
        null
    }
}
