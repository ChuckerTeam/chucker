package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers.Companion.headersOf
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test

internal class OkHttpUtilsTest {

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
        every { mockResponse.headers } returns headersOf("Content-Encoding", "gzip")

        assertThat(mockResponse.isGzipped).isTrue()
    }

    @Test
    fun responseIsGzipped_withOtherEncoding_returnsFalse() {
        val mockResponse = mockk<Response>()
        every { mockResponse.headers } returns headersOf("Content-Encoding", "identity")

        assertThat(mockResponse.isGzipped).isFalse()
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsTrue() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers } returns headersOf("Content-Encoding", "gzip")

        assertThat(mockRequest.isGzipped).isTrue()
    }

    @Test
    fun requestIsGzipped_withOtherEncoding_returnsFalse() {
        val mockRequest = mockk<Request>()
        every { mockRequest.headers } returns headersOf("Content-Encoding", "identity")

        assertThat(mockRequest.isGzipped).isFalse()
    }
}
