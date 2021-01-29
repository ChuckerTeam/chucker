package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

internal class OkioUtilsTest {
    @Test
    fun isProbablyPlainText_withEmptyBuffer_returnsTrue() {
        val buffer = Buffer()

        assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withWhiteSpace_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString(" ", Charset.defaultCharset())

        assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withPlainText_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString("just a string", Charset.defaultCharset())

        assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withCodePoint_returnsFalse() {
        val buffer = Buffer()
        buffer.writeByte(0x11000000)

        assertThat(buffer.isProbablyPlainText).isFalse()
    }

    @Test
    fun isPlaintext_withNonAsciiText_returnsTrue() {
        val buffer = Buffer()
        buffer.writeString("ą", Charset.defaultCharset())

        assertThat(buffer.isProbablyPlainText).isTrue()
    }

    @Test
    fun isPlaintext_withTruncatedUtf_returnsFalse() {
        val bytes = "ą".encodeUtf8().let { it.substring(0, it.size - 1) }
        val buffer = Buffer().write(bytes)

        assertThat(buffer.isProbablyPlainText).isFalse()
    }
}
