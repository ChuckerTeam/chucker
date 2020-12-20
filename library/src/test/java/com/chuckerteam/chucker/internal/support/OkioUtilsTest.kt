package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import okio.Buffer
import org.junit.jupiter.api.Test
import java.io.EOFException
import java.nio.charset.Charset

internal class OkioUtilsTest {
    @Test
    fun isPlaintext_withEmptyBuffer_returnsTrue() {
        val buffer = Buffer()

        Truth.assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withWhiteSpace_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString(" ", Charset.defaultCharset())

        Truth.assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withPlainText_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString("just a string", Charset.defaultCharset())

        Truth.assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withCodePoint_returnsFalse() {
        val buffer = Buffer()
        buffer.writeByte(0x11000000)

        Truth.assertThat(buffer.isProbablyPlainText).isFalse()
    }

    @Test
    fun isPlaintext_withEOF_returnsFalse() {
        val mockBuffer = mockk<Buffer>()
        every { mockBuffer.size } returns 100L
        every { mockBuffer.copyTo(any<Buffer>(), any(), any()) } throws EOFException()

        Truth.assertThat(mockBuffer.isProbablyPlainText).isFalse()
    }
}
