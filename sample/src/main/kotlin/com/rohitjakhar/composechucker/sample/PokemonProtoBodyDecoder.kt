package com.rohitjakhar.composechucker.sample

import com.chuckerteam.chucker.sample.Pokemon
import com.rohitjakhar.composechucker.api.BodyDecoder
import okhttp3.Request
import okio.ByteString

internal class PokemonProtoBodyDecoder : BodyDecoder {
    override fun decodeRequest(request: Request, body: ByteString): String? {
        return if (request.url.host.contains("postman", ignoreCase = true)) {
            Pokemon.ADAPTER.decode(body).toString()
        } else {
            null
        }
    }

    override fun decodeResponse(response: okhttp3.Response, body: ByteString): String? = null
}
