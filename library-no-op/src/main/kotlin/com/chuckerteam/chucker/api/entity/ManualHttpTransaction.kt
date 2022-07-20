package com.chuckerteam.chucker.api.entity

/**
 * No-op implementation.
 */
public data class ManualHttpTransaction(
    public var requestDate: Long?,
    public var responseDate: Long?,
    public var tookMs: Long?,
    public var protocol: String?,
    public var method: String?,
    public var url: String?,
    public var host: String?,
    public var path: String?,
    public var scheme: String?,
    public var responseTlsVersion: String?,
    public var responseCipherSuite: String?,
    public var requestPayloadSize: Long?,
    public var requestContentType: String?,
    public var requestHeaders: String?,
    public var requestHeadersSize: Long?,
    public var requestBody: String?,
    public var isRequestBodyEncoded: Boolean = false,
    public var responseCode: Int?,
    public var responseMessage: String?,
    public var error: String?,
    public var responsePayloadSize: Long?,
    public var responseContentType: String?,
    public var responseHeaders: String?,
    public var responseHeadersSize: Long?,
    public var responseBody: String?,
    public var isResponseBodyEncoded: Boolean = false,
    public var responseImageData: ByteArray?
)
