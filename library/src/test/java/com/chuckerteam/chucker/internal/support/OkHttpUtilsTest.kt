package com.chuckerteam.chucker.internal.support

import io.mockk.every
import io.mockk.mockk
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
    fun hasBody_withHeadMethod() {
        val mockResponse = mockk<Response>()
        val mockRequest = mockk<Request>()
        every { mockRequest.method() } returns "HEAD"
        every { mockResponse.request() } returns mockRequest

        assertFalse(mockResponse.hasBody())
    }

    @Test
    fun hasBody_with404_hasBody() {
        val mockResponse = mockk<Response>()
        val mockRequest = mockk<Request>()
        every { mockRequest.method() } returns ""
        every { mockResponse.request() } returns mockRequest
        every { mockResponse.code() } returns 404

        assertTrue(mockResponse.hasBody())
    }

    @Test
    fun hasBody_with204NoContent_doesNotHaveBody() {
        val mockResponse = mockk<Response>()
        val mockRequest = mockk<Request>()
        every { mockRequest.method() } returns ""
        every { mockResponse.request() } returns mockRequest
        every { mockResponse.code() } returns 204
        every { mockResponse.header("Content-Length") } returns null
        every { mockResponse.header("Transfer-Encoding") } returns null

        assertFalse(mockResponse.hasBody())
    }

    @Test
    fun hasBody_with304NotModified_doesNotHaveBody() {
        val mockResponse = mockk<Response>()
        val mockRequest = mockk<Request>()
        every { mockRequest.method() } returns ""
        every { mockResponse.request() } returns mockRequest
        every { mockResponse.code() } returns 304
        every { mockResponse.header("Content-Length") } returns null
        every { mockResponse.header("Transfer-Encoding") } returns null

        assertFalse(mockResponse.hasBody())
    }

    @Test
    fun hasBody_withMalformedRequest_doesHaveBody() {
        val mockResponse = mockk<Response>()
        val mockRequest = mockk<Request>()
        every { mockRequest.method() } returns ""
        every { mockResponse.request() } returns mockRequest
        every { mockResponse.code() } returns 304
        every { mockResponse.header("Content-Length") } returns "42"

        assertTrue(mockResponse.hasBody())
    }
}
