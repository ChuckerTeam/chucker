package com.chuckerteam.chucker.api

import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import okio.IOException

/**
 * Decodes HTTP request and response bodies to human–readable texts.
 */
public interface BodyDecoder {
    /**
     * Returns a text representation of [body] that will be displayed in Chucker UI transaction,
     * or `null` if [request] cannot be handled by this decoder. [Body][body] is no longer than
     * [max content length][ChuckerInterceptor.Builder.maxContentLength] and is guaranteed to be
     * uncompressed even if [request] has gzip or br header.
     */
    @Throws(IOException::class)
    public fun decodeRequest(request: Request, body: ByteString): String?

    /**
     * Returns a text representation of [body] that will be displayed in Chucker UI transaction,
     * or `null` if [response] cannot be handled by this decoder. [Body][body] is no longer than
     * [max content length][ChuckerInterceptor.Builder.maxContentLength] and is guaranteed to be
     * uncompressed even if [response] has gzip or br header.
     */
    @Throws(IOException::class)
    public fun decodeResponse(response: Response, body: ByteString): String?
}
