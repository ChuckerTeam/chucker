package com.chuckerteam.chucker.api

/**
 * No-op declaration.
 */
public data class ChuckerHttpTransaction(
    var method: String?,
    var scheme: String?,
    var host: String?,
    var path: String?,
    val responseCode: Int?,
    var requestDate: Long?,
    var tookMs: Long?,
)
