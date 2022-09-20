package com.chuckerteam.chucker.api.entity

/**
 * No-op implementation.
 */
public data class ManualHttpTransaction(
    public var requestDate: Long? = null,
    public var responseDate: Long? = null,
    public var tookMs: Long? = null,
    public var protocol: String? = null,
    public var method: String? = null,
    public var url: String? = null,
    public var host: String? = null,
    public var path: String? = null,
    public var scheme: String? = null,
    public var responseTlsVersion: String? = null,
    public var responseCipherSuite: String? = null,
    public var requestPayloadSize: Long? = null,
    public var requestContentType: String? = null,
    public var requestHeaders: String? = null,
    public var requestHeadersSize: Long? = null,
    public var requestBody: String? = null,
    public var isRequestBodyEncoded: Boolean = false,
    public var responseCode: Int? = null,
    public var responseMessage: String? = null,
    public var error: String? = null,
    public var responsePayloadSize: Long? = null,
    public var responseContentType: String? = null,
    public var responseHeaders: String? = null,
    public var responseHeadersSize: Long? = null,
    public var responseBody: String? = null,
    public var isResponseBodyEncoded: Boolean = false,
    public var responseImageData: ByteArray? = null
)
