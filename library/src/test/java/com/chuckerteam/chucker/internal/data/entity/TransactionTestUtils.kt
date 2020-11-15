package com.chuckerteam.chucker.internal.data.entity

import com.google.common.truth.Truth.assertThat
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.UUID

internal fun createRequest(path: String = ""): HttpTransaction =
    HttpTransaction().apply {
        setRequestHeaders(randomHeaders())
        populateUrl("https://www.example.com/$path?query=baz".toHttpUrlOrNull()!!)
        isRequestBodyPlainText = true
        requestDate = 300L
        method = "GET"
        requestContentType = "text/plain"
        requestPayloadSize = 0L
        requestBody = randomString()
    }

internal fun HttpTransaction.withResponseData(): HttpTransaction = this.apply {
    setResponseHeaders(randomHeaders())
    responseCode = 418 // I'm a teapot
    responseDate = 321L
    tookMs = 21L
    responseTlsVersion = randomString()
    responseCipherSuite = randomString()
    responsePayloadSize = 0L
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
    assertThat(actual.size).isEqualTo(expected.size)
    expected.forEachIndexed { index, expectedTransaction ->
        assertThat(actual[index].id).isEqualTo(expectedTransaction.id)
        assertTuple(expectedTransaction.id, expectedTransaction, actual[index])
    }
}

internal fun assertTuple(
    id: Long,
    expected: HttpTransaction,
    actual: HttpTransactionTuple?
) {
    assertThat(actual?.id).isEqualTo(id)
    assertThat(actual?.requestDate).isEqualTo(expected.requestDate)
    assertThat(actual?.method).isEqualTo(expected.method)
    assertThat(actual?.host).isEqualTo(expected.host)
    assertThat(actual?.path).isEqualTo(expected.path)
    assertThat(actual?.scheme).isEqualTo(expected.scheme)
    assertThat(actual?.responseCode).isEqualTo(expected.responseCode)
    assertThat(actual?.requestPayloadSize).isEqualTo(expected.requestPayloadSize)
    assertThat(actual?.responsePayloadSize).isEqualTo(expected.responsePayloadSize)
    assertThat(actual?.error).isEqualTo(expected.error)
}

internal fun assertTransaction(
    transactionId: Long,
    expected: HttpTransaction,
    actual: HttpTransaction?
) {
    assertThat(actual?.id).isEqualTo(transactionId)
    assertThat(actual?.url).isEqualTo(expected.url)
    assertThat(actual?.host).isEqualTo(expected.host)
    assertThat(actual?.path).isEqualTo(expected.path)
    assertThat(actual?.scheme).isEqualTo(expected.scheme)
    assertThat(actual?.requestHeaders).isEqualTo(expected.requestHeaders)
    assertThat(actual?.isRequestBodyPlainText).isEqualTo(expected.isRequestBodyPlainText)
    assertThat(actual?.requestDate).isEqualTo(expected.requestDate)
    assertThat(actual?.method).isEqualTo(expected.method)
    assertThat(actual?.requestContentType).isEqualTo(expected.requestContentType)
    assertThat(actual?.requestPayloadSize).isEqualTo(expected.requestPayloadSize)
    assertThat(actual?.requestBody).isEqualTo(expected.requestBody)
    assertThat(actual?.responseHeaders).isEqualTo(expected.responseHeaders)
    assertThat(actual?.responseCode).isEqualTo(expected.responseCode)
    assertThat(actual?.responseDate).isEqualTo(expected.responseDate)
    assertThat(actual?.tookMs).isEqualTo(expected.tookMs)
    assertThat(actual?.responseTlsVersion).isEqualTo(expected.responseTlsVersion)
    assertThat(actual?.responseCipherSuite).isEqualTo(expected.responseCipherSuite)
    assertThat(actual?.responsePayloadSize).isEqualTo(expected.responsePayloadSize)
    assertThat(actual?.requestContentType).isEqualTo(expected.requestContentType)
    assertThat(actual?.responseMessage).isEqualTo(expected.responseMessage)
    assertThat(actual?.responseBody).isEqualTo(expected.responseBody)
    assertThat(actual?.error).isEqualTo(expected.error)
}
