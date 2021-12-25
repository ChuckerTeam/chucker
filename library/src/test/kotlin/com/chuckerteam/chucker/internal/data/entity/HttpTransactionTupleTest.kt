package com.chuckerteam.chucker.internal.data.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class HttpTransactionTupleTest {
    @Test
    fun `transaction has SSL scheme`() {
        assertThat(createTuple(scheme = "https").isSsl).isTrue()
    }

    @Test
    fun `transaction does not have SSL scheme`() {
        assertThat(createTuple(scheme = "http").isSsl).isFalse()
    }

    @Test
    fun `transaction has complete status`() {
        assertThat(
            createTuple(
                responseCode = 200,
                error = null
            ).status
        ).isEqualTo(HttpTransaction.Status.Complete)
    }

    @Test
    fun `transaction has failure status`() {
        assertThat(
            createTuple(
                error = "foo"
            ).status
        ).isEqualTo(HttpTransaction.Status.Failed)
    }

    @Test
    fun `transaction has requested status`() {
        assertThat(
            createTuple(
                responseCode = null
            ).status
        ).isEqualTo(HttpTransaction.Status.Requested)
    }

    @Test
    fun `duration is null`() {
        assertThat(createTuple(tookMs = null).durationString).isNull()
    }

    @Test
    fun `duration is formatted`() {
        assertThat(createTuple(tookMs = 123).durationString).isEqualTo("123 ms")
    }

    @Test
    fun `total size forces null request and response payload sizes to be 0 bytes`() {
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
    fun `total size adds request and response payload sizes`() {
        assertThat(
            createTuple(
                requestPayloadSize = 123,
                responsePayloadSize = 456
            ).totalSizeString
        ).isEqualTo("579 B")
    }

    @Test
    fun `formatted path is not encoded`() {
        assertThat(createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(encode = false))
            .isEqualTo("/abc/def?foo=bar baz")
    }

    @Test
    fun `formatted path is encoded`() {
        assertThat(createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(encode = true))
            .isEqualTo("/abc/def?foo=bar%20baz")
    }

    @Test
    fun `formatted path handles null path`() {
        assertThat(createTuple(path = null).getFormattedPath(encode = false))
            .isEqualTo("")
        assertThat(createTuple(path = null).getFormattedPath(encode = true))
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
