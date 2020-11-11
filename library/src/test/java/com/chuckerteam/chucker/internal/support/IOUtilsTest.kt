package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okio.Buffer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.EOFException
import java.nio.charset.Charset
import java.util.stream.Stream

internal class IOUtilsTest {

    private val mockContext = mockk<Context>()
    private val ioUtils = IOUtils(mockContext)

    @Test
    fun isPlaintext_withEmptyBuffer_returnsTrue() {
        val buffer = Buffer()

        assertThat(ioUtils.isPlaintext(buffer)).isTrue()
    }

    @Test
    fun isPlaintext_withWhiteSpace_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString(" ", Charset.defaultCharset())

        assertThat(ioUtils.isPlaintext(buffer)).isTrue()
    }

    @Test
    fun isPlaintext_withPlainText_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString("just a string", Charset.defaultCharset())

        assertThat(ioUtils.isPlaintext(buffer)).isTrue()
    }

    @Test
    fun isPlaintext_withCodepoint_returnsFalse() {
        val buffer = Buffer()
        buffer.writeByte(0x11000000)

        assertThat(ioUtils.isPlaintext(buffer)).isFalse()
    }

    @Test
    fun isPlaintext_withEOF_returnsFalse() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size } returns 100L
        every { mockBuffer.copyTo(any<Buffer>(), any(), any()) } throws EOFException()

        assertThat(ioUtils.isPlaintext(mockBuffer)).isFalse()
    }

    @Test
    fun readFromBuffer_contentNotTruncated() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size } returns 100L
        every { mockBuffer.readString(any(), any()) } returns "{ \"message\": \"just a mock body\"}"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 200L)

        assertThat(result).isEqualTo("{ \"message\": \"just a mock body\"}")
        verify { mockBuffer.readString(100L, Charset.defaultCharset()) }
    }

    @Test
    fun readFromBuffer_contentTruncated() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size } returns 100L
        every { mockBuffer.readString(any(), any()) } returns "{ \"message\": \"just a mock body\"}"
        every { mockContext.getString(R.string.chucker_body_content_truncated) } returns "\\n\\n--- Content truncated ---"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 50L)

        assertThat(result).isEqualTo("{ \"message\": \"just a mock body\"}\\n\\n--- Content truncated ---")
        verify { mockBuffer.readString(50L, Charset.defaultCharset()) }
    }

    @Test
    fun readFromBuffer_unexpectedEOF() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size } returns 100L
        every { mockBuffer.readString(any(), any()) } throws EOFException()
        every { mockContext.getString(R.string.chucker_body_unexpected_eof) } returns "\\n\\n--- Unexpected end of content ---"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 200L)

        assertThat(result).isEqualTo("\\n\\n--- Unexpected end of content ---")
    }

    @Test
    fun getNativeSource_withNotGzipped() {
        val buffer = Buffer()

        val nativeSource = ioUtils.getNativeSource(buffer, false)
        assertThat(nativeSource).isEqualTo(buffer)
    }

    @Test
    fun getNativeSource_withGzipped() {
        val buffer = Buffer()

        val nativeSource = ioUtils.getNativeSource(buffer, true)
        assertThat(nativeSource).isNotEqualTo(buffer)
    }

    @ParameterizedTest(name = "{0} must be supported? {1}")
    @MethodSource("supportedEncodingSource")
    @DisplayName("Check if body encoding is supported")
    fun bodyHasSupportedEncoding(encoding: String?, isSupported: Boolean) {
        val result = ioUtils.bodyHasSupportedEncoding(encoding)

        assertThat(result).isEqualTo(isSupported)
    }

    companion object {
        @JvmStatic
        fun supportedEncodingSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null, true),
                Arguments.of("", true),
                Arguments.of("identity", true),
                Arguments.of("gzip", true),
                Arguments.of("other", false)
            )
        }
    }
}
