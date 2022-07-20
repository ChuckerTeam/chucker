package com.chuckerteam.chucker.api.entity

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

/**
 * Represent Manual Http transaction that developer want to populate as an custom http transaction
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
) {

    /**
     * this will convert this class to HttpTransaction to be able to save it on database as a real
     * transaction.
     */
    internal fun convertToHttpTransaction(): HttpTransaction {
        return HttpTransaction(
            0,
            requestDate,
            responseDate,
            tookMs,
            protocol,
            method,
            url,
            host,
            path,
            scheme,
            responseTlsVersion,
            responseCipherSuite,
            requestPayloadSize,
            requestContentType,
            requestHeaders,
            requestHeadersSize,
            requestBody,
            isRequestBodyEncoded,
            responseCode,
            responseMessage,
            error,
            responsePayloadSize,
            responseContentType,
            responseHeaders,
            responseHeadersSize,
            responseBody,
            isResponseBodyEncoded,
            responseImageData
        )
    }
}
