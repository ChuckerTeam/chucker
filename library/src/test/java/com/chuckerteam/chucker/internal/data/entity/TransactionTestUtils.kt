package com.chuckerteam.chucker.internal.data.entity

import java.util.UUID
import okhttp3.Headers
import okhttp3.HttpUrl
import org.junit.Assert.assertEquals

internal fun createRequest(path: String = ""): HttpTransaction =
    HttpTransaction().apply {
        setRequestHeaders(randomHeaders())
        populateUrl(HttpUrl.parse("https://www.example.com/$path?query=baz")!!)
        isRequestBodyPlainText = true
        requestDate = 300L
        method = "GET"
        requestContentType = "text/plain"
        requestContentLength = 0L
        requestBody = randomString()
    }

internal fun HttpTransaction.withResponseData(): HttpTransaction = this.apply {
    setResponseHeaders(randomHeaders())
    responseCode = 418 // I'm a teapot
    responseDate = 321L
    tookMs = 21L
    responseTlsVersion = randomString()
    responseCipherSuite = randomString()
    responseContentLength = 0L
    requestContentType = randomString()
    responseMessage = randomString()
    responseBody = randomString()
    error = randomString()
}

private fun randomHeaders(): Headers = Headers.Builder()
    .add("name-one", randomString())
    .add("name-two", randomString())
    .add("Content-Encoding", "gzip")
    .build()

internal fun randomString() = UUID.randomUUID().toString()

internal fun assertTuples(
    expected: List<HttpTransaction>,
    actual: List<HttpTransactionTuple>
) {
    assertEquals(expected.size, actual.size)
    expected.forEachIndexed { index, expectedTransaction ->
        assertEquals(expectedTransaction.id, actual[index].id)
        assertTuple(expectedTransaction.id, expectedTransaction, actual[index])
    }
}

internal fun assertTuple(
    id: Long,
    expected: HttpTransaction,
    actual: HttpTransactionTuple?
) {
    assertEquals(id, actual?.id)
    assertEquals(expected.requestDate, actual?.requestDate)
    assertEquals(expected.method, actual?.method)
    assertEquals(expected.host, actual?.host)
    assertEquals(expected.path, actual?.path)
    assertEquals(expected.scheme, actual?.scheme)
    assertEquals(expected.responseCode, actual?.responseCode)
    assertEquals(expected.requestContentLength, actual?.requestContentLength)
    assertEquals(expected.responseContentLength, actual?.responseContentLength)
    assertEquals(expected.error, actual?.error)
}

internal fun assertTransaction(
    transactionId: Long,
    expected: HttpTransaction,
    actual: HttpTransaction?
) {
    assertEquals(transactionId, actual?.id)
    assertEquals(expected.url, actual?.url)
    assertEquals(expected.host, actual?.host)
    assertEquals(expected.path, actual?.path)
    assertEquals(expected.scheme, actual?.scheme)
    assertEquals(expected.requestHeaders, actual?.requestHeaders)
    assertEquals(expected.isRequestBodyPlainText, actual?.isRequestBodyPlainText)
    assertEquals(expected.requestDate, actual?.requestDate)
    assertEquals(expected.method, actual?.method)
    assertEquals(expected.requestContentType, actual?.requestContentType)
    assertEquals(expected.requestContentLength, actual?.requestContentLength)
    assertEquals(expected.requestBody, actual?.requestBody)
    assertEquals(expected.responseHeaders, actual?.responseHeaders)
    assertEquals(expected.responseCode, actual?.responseCode)
    assertEquals(expected.responseDate, actual?.responseDate)
    assertEquals(expected.tookMs, actual?.tookMs)
    assertEquals(expected.responseTlsVersion, actual?.responseTlsVersion)
    assertEquals(expected.responseCipherSuite, actual?.responseCipherSuite)
    assertEquals(expected.responseContentLength, actual?.responseContentLength)
    assertEquals(expected.requestContentType, actual?.requestContentType)
    assertEquals(expected.responseMessage, actual?.responseMessage)
    assertEquals(expected.responseBody, actual?.responseBody)
    assertEquals(expected.error, actual?.error)
}
