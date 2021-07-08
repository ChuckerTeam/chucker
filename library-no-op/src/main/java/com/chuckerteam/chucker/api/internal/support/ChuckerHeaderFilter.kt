package com.chuckerteam.chucker.api.internal.support

import okhttp3.Request

internal object ChuckerHeaderFilter {

    private const val CHUCKER_HEADER_PREFIX = "chucker-"

    fun removeAllChuckerHeadersFromRequest(request: Request): Request {
        val chuckerHeaders = request.headers.filter { it.first.startsWith(CHUCKER_HEADER_PREFIX) }
        var headers = request.headers
        chuckerHeaders.forEach {
            headers = request.headers.newBuilder().removeAll(it.first).build()
        }
        return request.newBuilder().headers(headers).build()
    }
}
