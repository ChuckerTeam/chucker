package com.chuckerteam.chucker.api

import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import okio.IOException

/**
 * No-op declaration
 */
public interface BodyDecoder {
    @Throws(IOException::class)
    public fun decodeRequest(request: Request, body: ByteString): String?

    @Throws(IOException::class)
    public fun decodeResponse(response: Response, body: ByteString): String?
}
