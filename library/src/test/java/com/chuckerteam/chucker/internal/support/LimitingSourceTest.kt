package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.SEGMENT_SIZE
import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.buffer
import org.junit.jupiter.api.Test

internal class LimitingSourceTest {
    @Test
    fun limitedBytes_doNotExceedLimit_whenAboveThreshold() {
        val content = "!".repeat(10 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        val limitedContent = limitingSource.buffer().readByteString().utf8()

        assertThat(limitedContent).isEqualTo("!".repeat(2 * SEGMENT_SIZE.toInt()))
    }

    @Test
    fun originalBytes_areNotDepleted() {
        val content = "!".repeat(10 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()
        val originalContent = originalSource.buffer.readByteString().utf8()

        assertThat(originalContent).isEqualTo("!".repeat(8 * SEGMENT_SIZE.toInt()))
    }

    @Test
    fun bytesLimitIsNotReached_whenBelowThreshold() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 4 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isFalse()
    }

    @Test
    fun bytesLimitIsReached_whenEqualToThreshold() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 3 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isTrue()
    }

    @Test
    fun bytesLimitIsReached_whenAboveThreshold() {
        val content = "!".repeat(3 * SEGMENT_SIZE.toInt())
        val originalSource = Buffer().writeUtf8(content)
        val limitingSource = LimitingSource(originalSource, 2 * SEGMENT_SIZE)

        limitingSource.buffer().readByteString()

        assertThat(limitingSource.isThresholdReached).isTrue()
    }
}
