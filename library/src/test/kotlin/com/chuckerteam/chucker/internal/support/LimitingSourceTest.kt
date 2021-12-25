package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.util.SEGMENT_SIZE
import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.buffer
import org.junit.jupiter.api.Test

internal class LimitingSourceTest {
    @Test
    fun `upstream bytes do not exceed a limit`() {
        val content = "!".repeat(10 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        val limitedContent = limitingSource.buffer().readByteString().utf8()

        assertThat(limitedContent).isEqualTo("!".repeat(2 * SEGMENT_SIZE.toInt()))
    }

    @Test
    fun `upstream byte over a limit are not depleted`() {
        val content = "!".repeat(10 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()
        val originalContent = originalSource.buffer.readByteString().utf8()

        assertThat(originalContent).isEqualTo("!".repeat(8 * SEGMENT_SIZE.toInt()))
    }

    @Test
    fun `byte read limit is not reached with byte count under limit`() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 4 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isFalse()
    }

    @Test
    fun `byte read limit is reached with exact byte count`() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 3 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isTrue()
    }

    @Test
    fun `byte read limit is reached with byte count over limit`() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isTrue()
    }
}
