package com.chuckerteam.chucker.internal.support

import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OkHttpUtilsTest {

    @Test
    fun contentLength_withNoHeader_returnsInvalidValue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns null

        assertEquals(-1, mockResponse.contentLenght)
    }

    @Test
    fun contentLength_withZeroLenght_returnsZero() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns "0"

        assertEquals(0L, mockResponse.contentLenght)
    }

    @Test
    fun contentLength_withRealLenght_returnsValue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns "42"

        assertEquals(42L, mockResponse.contentLenght)
    }

    @Test
    fun isChunked_withNotChunked() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "gzip"

        assertFalse(mockResponse.isChunked)
    }

    @Test
    fun isChunked_withNoTransferEncoding() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns null
        every { mockResponse.header("Transfer-Encoding") } returns null

        assertFalse(mockResponse.isChunked)
    }

    @Test
    fun isChunked_withChunked() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "chunked"

        assertTrue(mockResponse.isChunked)
    }

    @Test
    fun responseIsGzipped_withOtherEncoding_returnsTrue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.headers() } returns Headers.of("Content-Encoding", "gzip")

        assertTrue(mockResponse.isGzipped)
    }

    @Test
    fun responseIsGzipped_withOtherEncoding_returnsFalse() {
        val mockResponse = mockk<Response>()
        every { mockResponse.headers() } returns Headers.of("Content-Encoding", "identity")

        assertFalse(mockResponse.isGzipped)
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsTrue() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers() } returns Headers.of("Content-Encoding", "gzip")

        assertTrue(mockRequest.isGzipped)
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsFalse() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers() } returns Headers.of("Content-Encoding", "identity")

        assertFalse(mockRequest.isGzipped)
    }
}
