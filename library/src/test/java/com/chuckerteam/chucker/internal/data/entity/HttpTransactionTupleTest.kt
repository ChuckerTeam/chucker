package com.chuckerteam.chucker.internal.data.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpTransactionTupleTest {
    @Test
    fun schemeIsSSL() {
        assertTrue(createTuple(scheme = "https").isSsl)
    }

    @Test
    fun schemeIsNotSSL() {
        assertFalse(createTuple(scheme = "http").isSsl)
    }

    @Test
    fun statusIsComplete() {
        assertEquals(
            HttpTransaction.Status.Complete,
            createTuple(responseCode = 200, error = null).status
        )
    }

    @Test
    fun statusIsFailed() {
        assertEquals(
            HttpTransaction.Status.Failed,
            createTuple(error = "foo").status
        )
    }

    @Test
    fun statusIsRequested() {
        assertEquals(
            HttpTransaction.Status.Requested,
            createTuple(responseCode = null).status
        )
    }

    @Test
    fun durationBlankUntilWeHaveData() {
        assertNull(createTuple(tookMs = null).durationString)
    }

    @Test
    fun duration() {
        assertEquals("123 ms", createTuple(tookMs = 123).durationString)
    }

    @Test
    fun totalSizeHandlesNulls() {
        assertEquals(
            "0 B",
            createTuple(
                requestContentLength = null,
                responseContentLength = null
            ).totalSizeString
        )

        assertEquals(
            "0 B",
            createTuple(
                requestContentLength = 0,
                responseContentLength = null
            ).totalSizeString
        )

        assertEquals(
            "0 B",
            createTuple(
                requestContentLength = null,
                responseContentLength = 0
            ).totalSizeString
        )
    }

    @Test
    fun totalSize() {
        assertEquals(
            "579 B",
            createTuple(
                requestContentLength = 123,
                responseContentLength = 456
            ).totalSizeString
        )
    }

    @Test
    fun nonEncodedFormattedPath() {
        assertEquals("/abc/def?foo=bar baz", createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(false))
    }

    @Test
    fun encodedFormattedPath() {
        assertEquals("/abc/def?foo=bar%20baz", createTuple(path = "/abc/def?foo=bar baz").getFormattedPath(true))
    }

    @Test
    fun formattedPathHandlesNull() {
        assertEquals("", createTuple(path = null).getFormattedPath(false))
        assertEquals("", createTuple(path = null).getFormattedPath(true))
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
        requestContentLength: Long? = null,
        responseContentLength: Long? = null,
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
        requestContentLength,
        responseContentLength,
        error
    )
}
