package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.decodeHex
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
    fun `gzip compressed response is uncompressed`() {
        val content = "Hello there!"
        val source = Buffer()
        GzipSink(source).buffer().use { it.writeUtf8(content) }

        val result = source.uncompress(headersOf("Content-Encoding", "gzip"))
            .buffer()
            .use(BufferedSource::readUtf8)

        assertThat(result).isEqualTo(content)
    }

    @Test
    fun `brotli compressed response is uncompressed`() {
        val brotliEncodedString =
            "1bce00009c05ceb9f028d14e416230f718960a537b0922d2f7b6adef56532c08dff44551516690131494db" +
                "6021c7e3616c82c1bc2416abb919aaa06e8d30d82cc2981c2f5c900bfb8ee29d5c03deb1c0dacff80e" +
                "abe82ba64ed250a497162006824684db917963ecebe041b352a3e62d629cc97b95cac24265b175171e" +
                "5cb384cd0912aeb5b5dd9555f2dd1a9b20688201"

        val brotliSource = Buffer().write(brotliEncodedString.decodeHex())

        val result = brotliSource.uncompress(headersOf("Content-Encoding", "br"))
            .buffer()
            .use(BufferedSource::readUtf8)

        assertThat(result).contains("\"brotli\": true,")
        assertThat(result).contains("\"Accept-Encoding\": \"br\"")
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
            "br" to true,
            "identity" to true,
            "gzip" to true,
            "other" to false,
        ).map { (encoding, result) ->
            val headers = if (encoding == null) headersOf() else headersOf("Content-Encoding", encoding)
            Arguments.of(headers, result)
        }
    }
}
