package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import okio.GzipSink
import okio.buffer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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

    @Test
    fun uncompressSource_withGzippedContent() {
        val content = "Hello there!"
        val source = Buffer()
        GzipSink(source).buffer().use { it.writeUtf8(content) }

        val result = source.uncompress(headersOf("Content-Encoding", "gzip"))
            .buffer()
            .use(BufferedSource::readUtf8)

        assertThat(result).isEqualTo(content)
    }

    @Test
    fun uncompressSource_withPlainTextContent() {
        val content = "Hello there!"
        val source = Buffer().writeUtf8(content)

        val result = source.uncompress(headersOf())
            .buffer()
            .use(BufferedSource::readUtf8)

        assertThat(result).isEqualTo(content)
    }

    @ParameterizedTest(name = "\"{0}\" must be supported: {1}")
    @MethodSource("supportedEncodingSource")
    @DisplayName("Check if body encoding is supported")
    fun headersHaveSupportedEncoding(headers: Headers, isSupported: Boolean) {
        val result = headers.hasSupportedContentEncoding

        assertThat(result).isEqualTo(isSupported)
    }

    companion object {
        @JvmStatic
        fun supportedEncodingSource(): Stream<Arguments> = Stream.of(
            null to true,
            "" to true,
            "identity" to true,
            "gzip" to true,
            "other" to false,
        ).map { (encoding, result) ->
            val headers = if (encoding == null) headersOf() else headersOf("Content-Encoding", encoding)
            Arguments.of(headers, result)
        }
    }
}
