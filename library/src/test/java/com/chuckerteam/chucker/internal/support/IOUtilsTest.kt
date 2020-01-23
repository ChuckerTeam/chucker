package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.EOFException
import java.nio.charset.Charset
import java.util.stream.Stream
import okio.Buffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class IOUtilsTest {

    private val mockContext = mockk<Context>()
    private val ioUtils = IOUtils(mockContext)

    @Test
    fun isPlaintext_withEmptyBuffer_returnsTrue() {
        val buffer = Buffer()

        assertTrue(ioUtils.isPlaintext(buffer))
    }

    @Test
    fun isPlaintext_withWhiteSpace_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString(" ", Charset.defaultCharset())

        assertTrue(ioUtils.isPlaintext(buffer))
    }

    @Test
    fun isPlaintext_withPlainText_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString("just a string", Charset.defaultCharset())

        assertTrue(ioUtils.isPlaintext(buffer))
    }

    @Test
    fun isPlaintext_withCodepoint_returnsFalse() {
        val buffer = Buffer()
        buffer.writeByte(0x11000000)

        assertFalse(ioUtils.isPlaintext(buffer))
    }

    @Test
    fun isPlaintext_withEOF_returnsFalse() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size() } returns 100L
        every { mockBuffer.copyTo(any<Buffer>(), any(), any()) } throws EOFException()

        assertFalse(ioUtils.isPlaintext(mockBuffer))
    }

    fun readFromBuffer_contentNotTruncated() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size() } returns 100L
        every { mockBuffer.readString(any(), any()) } returns "{ \"message\": \"just a mock body\"}"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 200L)

        assertEquals("{ \"message\": \"just a mock body\"}", result)
        verify { mockBuffer.readString(100L, Charset.defaultCharset()) }
    }

    @Test
    fun readFromBuffer_contentTruncated() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size() } returns 100L
        every { mockBuffer.readString(any(), any()) } returns "{ \"message\": \"just a mock body\"}"
        every { mockContext.getString(R.string.chucker_body_content_truncated) } returns "\\n\\n--- Content truncated ---"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 50L)

        assertEquals("{ \"message\": \"just a mock body\"}\\n\\n--- Content truncated ---", result)
        verify { mockBuffer.readString(50L, Charset.defaultCharset()) }
    }

    @Test
    fun readFromBuffer_unexpectedEOF() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size() } returns 100L
        every { mockBuffer.readString(any(), any()) } throws EOFException()
        every { mockContext.getString(R.string.chucker_body_unexpected_eof) } returns "\\n\\n--- Unexpected end of content ---"

        val result = ioUtils.readFromBuffer(mockBuffer, Charset.defaultCharset(), 200L)

        assertEquals("\\n\\n--- Unexpected end of content ---", result)
    }

    @Test
    fun getNativeSource_withNotGzipped() {
        val mockBuffer = mockk<Buffer>()

        val nativeSource = ioUtils.getNativeSource(mockBuffer, false)

        assertEquals(mockBuffer, nativeSource)
    }

    @Test
    fun getNativeSource_withGzipped() {
        val mockBuffer = mockk<Buffer>()

        val nativeSource = ioUtils.getNativeSource(mockBuffer, true)

        assertNotEquals(mockBuffer, nativeSource)
    }

    @ParameterizedTest(name = "{0} must be supported? {1}")
    @MethodSource("supportedEncodingSource")
    @DisplayName("Check if body encoding is supported")
    fun bodyHasSupportedEncoding(encoding: String?, supported: Boolean) {
        val result = ioUtils.bodyHasSupportedEncoding(encoding)

        assertEquals(supported, result)
    }

    @Test
    fun bodyIsGzipped_withGzip_returnsTrue() {
        assertTrue(ioUtils.bodyIsGzipped("gzip"))
    }

    @Test
    fun bodyIsGzipped_withOtherEncoding_returnsFalse() {
        assertFalse(ioUtils.bodyIsGzipped("other"))
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
