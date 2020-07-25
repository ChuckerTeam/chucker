package com.chuckerteam.chucker.internal.data.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HttpTransactionTupleTest {
    @Test
    fun schemeIsSSL() {
        assertThat(createTuple(scheme = "https").isSsl).isTrue()
    }

    @Test
    fun schemeIsNotSSL() {
        assertThat(createTuple(scheme = "http").isSsl).isFalse()
    }

    @Test
    fun statusIsComplete() {
        assertThat(
            createTuple(
                responseCode = 200,
                error = null
            ).status
        ).isEqualTo(HttpTransaction.Status.Complete)
    }

    @Test
    fun statusIsFailed() {
        assertThat(
            createTuple(
                error = "foo"
            ).status
        ).isEqualTo(HttpTransaction.Status.Failed)
    }

    @Test
    fun statusIsRequested() {
        assertThat(
            createTuple(
                responseCode = null
            ).status
        ).isEqualTo(HttpTransaction.Status.Requested)
    }

    @Test
    fun durationBlankUntilWeHaveData() {
        assertThat(createTuple(tookMs = null).durationString).isNull()
    }

    @Test
    fun duration() {
        assertThat(createTuple(tookMs = 123).durationString).isEqualTo("123 ms")
    }

    @Test
    fun totalSizeHandlesNulls() {
        assertThat(
            createTuple(
                requestPayloadSize = null,
                responsePayloadSize = null
            ).totalSizeString
        ).isEqualTo("0 B")

        assertThat(
            createTuple(
                requestPayloadSize = 0,
                responsePayloadSize = null
            ).totalSizeString
        ).isEqualTo("0 B")

        assertThat(
            createTuple(
                requestPayloadSize = null,
                responsePayloadSize = 0
            ).totalSizeString
        ).isEqualTo("0 B")
    }

    @Test
    fun totalSize() {
        assertThat(
            createTuple(
                requestPayloadSize = 123,
                responsePayloadSize = 456
            ).totalSizeString
        ).isEqualTo("579 B")
    }

    @Test
    fun nonEncodedFormattedPath() {
        assertThat(createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(false))
            .isEqualTo("/abc/def?foo=bar baz")
    }

    @Test
    fun encodedFormattedPath() {
        assertThat(createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(true))
            .isEqualTo("/abc/def?foo=bar%20baz")
    }

    @Test
    fun formattedPathHandlesNull() {
        assertThat(createTuple(path = null).getFormattedPath(false))
            .isEqualTo("")
        assertThat(createTuple(path = null).getFormattedPath(true))
            .isEqualTo("")
    }

    private fun createTuple(
        id: Long = 0,
        requestDate: Long? = null,
        tookMs: Long? = null,
        protocol: String? = null,
        method: String? = null,
        host: String? = null,
        path: String? = null,
        scheme: String? = null,
        responseCode: Int? = null,
        requestPayloadSize: Long? = null,
        responsePayloadSize: Long? = null,
        error: String? = null
    ) = HttpTransactionTuple(
        id,
        requestDate,
        tookMs,
        protocol,
        method,
        host,
        path,
        scheme,
        responseCode,
        requestPayloadSize,
        responsePayloadSize,
        error
    )
}
