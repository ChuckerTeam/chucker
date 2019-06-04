package com.chuckerteam.chucker.api.internal.support

import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when` as whenever
import org.mockito.Mockito.mock

class OkHttpUtilsTest {

    @Test
    fun contentLength_withNoHeader_returnsInvalidValue() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Content-Length")).thenReturn(null)

        assertEquals(-1, mockResponse.contentLenght)
    }

    @Test
    fun contentLength_withZeroLenght_returnsZero() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Content-Length")).thenReturn("0")

        assertEquals(0L, mockResponse.contentLenght)
    }

    @Test
    fun contentLength_withRealLenght_returnsValue() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Content-Length")).thenReturn("42")

        assertEquals(42L, mockResponse.contentLenght)
    }

    @Test
    fun isChunked_withNotChunked() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Transfer-Encoding")).thenReturn("gzip")

        assertEquals(false, mockResponse.isChunked)
    }

    @Test
    fun isChunked_withNoTransferEncoding() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Content-Length")).thenReturn(null)

        assertEquals(false, mockResponse.isChunked)
    }

    @Test
    fun isChunked_withChunked() {
        val mockResponse = mock(Response::class.java)
        whenever(mockResponse.header("Transfer-Encoding")).thenReturn("chunked")

        assertEquals(true, mockResponse.isChunked)
    }

    @Test
    fun hasBody_withHeadMethod() {
        val mockResponse = mock(Response::class.java)
        val mockRequest = mock(Request::class.java)
        whenever(mockRequest.method()).thenReturn("HEAD")
        whenever(mockResponse.request()).thenReturn(mockRequest)

        assertEquals(false, mockResponse.hasBody())
    }

    @Test
    fun hasBody_with404_hasBody() {
        val mockResponse = mock(Response::class.java)
        val mockRequest = mock(Request::class.java)
        whenever(mockRequest.method()).thenReturn("")
        whenever(mockResponse.request()).thenReturn(mockRequest)
        whenever(mockResponse.code()).thenReturn(404)

        assertEquals(true, mockResponse.hasBody())
    }

    @Test
    fun hasBody_with204NoContent_doesNotHaveBody() {
        val mockResponse = mock(Response::class.java)
        val mockRequest = mock(Request::class.java)
        whenever(mockRequest.method()).thenReturn("")
        whenever(mockResponse.request()).thenReturn(mockRequest)
        whenever(mockResponse.code()).thenReturn(204)

        assertEquals(false, mockResponse.hasBody())
    }

    @Test
    fun hasBody_with304NotModified_doesNotHaveBody() {
        val mockResponse = mock(Response::class.java)
        val mockRequest = mock(Request::class.java)
        whenever(mockRequest.method()).thenReturn("")
        whenever(mockResponse.request()).thenReturn(mockRequest)
        whenever(mockResponse.code()).thenReturn(304)

        assertEquals(false, mockResponse.hasBody())
    }

    @Test
    fun hasBody_withMalformedRequest_doesHaveBody() {
        val mockResponse = mock(Response::class.java)
        val mockRequest = mock(Request::class.java)
        whenever(mockRequest.method()).thenReturn("")
        whenever(mockResponse.request()).thenReturn(mockRequest)
        whenever(mockResponse.code()).thenReturn(304)
        whenever(mockResponse.header("Content-Length")).thenReturn("42")

        assertEquals(true, mockResponse.hasBody())
    }
}