package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
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
    fun `response is not chunked without chunked encoding`() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "gzip"

        assertThat(mockResponse.isChunked).isFalse()
    }

    @Test
    fun `response is not chunked with no encoding`() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Content-Length") } returns null
        every { mockResponse.header("Transfer-Encoding") } returns null

        assertThat(mockResponse.isChunked).isFalse()
    }

    @Test
    fun `response is chunked with chunked encoding`() {
        val mockResponse = mockk<Response>()
        every { mockResponse.header("Transfer-Encoding") } returns "chunked"

        assertThat(mockResponse.isChunked).isTrue()
    }

    @Test
    fun `gizpped response is gunzipped`() {
        val content = "Hello there!"
        val source = Buffer()
        GzipSink(source).buffer().use { it.writeUtf8(content) }

        val result = source.uncompress(headersOf("Content-Encoding", "gzip"))
            .buffer()
            .use(BufferedSource::readUtf8)

        assertThat(result).isEqualTo(content)
    }

    @Test
    fun `plain text response is not affected by uncompressing`() {
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
    fun `recognizes supported encodings`(headers: Headers, isSupported: Boolean) {
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
