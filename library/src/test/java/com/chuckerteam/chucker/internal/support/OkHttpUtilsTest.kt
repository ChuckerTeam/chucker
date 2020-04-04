package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test

class OkHttpUtilsTest {

    @Test
    fun contentLength_withNoHeader_returnsInvalidValue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns null

        assertThat(mockResponse.contentLength).isEqualTo(-1)
    }

    @Test
    fun contentLength_withZeroLength_returnsZero() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns "0"

        assertThat(mockResponse.contentLength).isEqualTo(0L)
    }

    @Test
    fun contentLength_withRealLength_returnsValue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns "42"

        assertThat(mockResponse.contentLength).isEqualTo(42L)
    }

    @Test
    fun isChunked_withNotChunked() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "gzip"

        assertThat(mockResponse.isChunked).isFalse()
    }

    @Test
    fun isChunked_withNoTransferEncoding() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns null
        every { mockResponse.header("Transfer-Encoding") } returns null

        assertThat(mockResponse.isChunked).isFalse()
    }

    @Test
    fun isChunked_withChunked() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "chunked"

        assertThat(mockResponse.isChunked).isTrue()
    }

    @Test
    fun responseIsGzipped_withOtherEncoding_returnsTrue() {
        val mockResponse = mockk<Response>()
        every { mockResponse.headers() } returns Headers.of("Content-Encoding", "gzip")

        assertThat(mockResponse.isGzipped).isTrue()
    }

    @Test
    fun responseIsGzipped_withOtherEncoding_returnsFalse() {
        val mockResponse = mockk<Response>()
        every { mockResponse.headers() } returns Headers.of("Content-Encoding", "identity")

        assertThat(mockResponse.isGzipped).isFalse()
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsTrue() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers() } returns Headers.of("Content-Encoding", "gzip")

        assertThat(mockRequest.isGzipped).isTrue()
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsFalse() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers() } returns Headers.of("Content-Encoding", "identity")

        assertThat(mockRequest.isGzipped).isFalse()
    }
}
