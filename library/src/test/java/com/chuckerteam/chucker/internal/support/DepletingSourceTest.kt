package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.IOException

internal class DepletingSourceTest {
    @Test
    fun delegateContent_isMovedToDownstream() {
        val delegate = Buffer().writeUtf8("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        val content = depletingSource.buffer().use(BufferedSource::readByteString)

        assertThat(content.utf8()).isEqualTo("Hello, world!")
    }

    @Test
    fun delegateIsNotDepleted_whenReadingFails() {
        val delegate = ThrowOnFirstReadSource("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        val exception = assertThrows<IOException> {
            // Because delegate throws only on a first read, this also checks if DepletingSource
            // does not try to read during close if a failure happened while reading.
            depletingSource.use { it.read(Buffer(), 1) }
        }
        assertThat(exception.message).isEqualTo("Hello there!")

        assertThat(delegate.content).isEqualTo("Hello, world!")
    }

    @Test
    fun delegateIsDepleted_whenSourceIsClosed() {
        val delegate = Buffer().writeUtf8("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        depletingSource.close()

        assertThat(delegate.snapshot()).isEqualTo(ByteString.EMPTY)
    }

    @Test
    fun readingFailures_areNotPropagated_whenSourceIsClosed() {
        val delegate = ThrowOnFirstReadSource("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        assertDoesNotThrow(depletingSource::close)
    }

    @Test
    fun delegateIsNotDepleted_whenReadingFails_duringClose() {
        val delegate = ThrowOnFirstReadSource("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        depletingSource.close()

        assertThat(delegate.content).isEqualTo("Hello, world!")
    }

    @Test
    fun delegateCannotBeDepleted_multipleTimes() {
        val delegate = Buffer().writeUtf8("Hello, world!")
        val depletingSource = DepletingSource(delegate)

        depletingSource.close()
        delegate.writeUtf8("And goodnight!")
        depletingSource.close()

        assertThat(delegate.snapshot().utf8()).isEqualTo("And goodnight!")
    }

    private class ThrowOnFirstReadSource(content: String) : Source {
        private val source = Buffer().writeUtf8(content)
        val content: String get() = source.snapshot().utf8()
        private var shouldThrow = true

        override fun read(sink: Buffer, byteCount: Long): Long {
            if (shouldThrow) {
                shouldThrow = false
                throw IOException("Hello there!")
            }
            return source.read(sink, byteCount)
        }

        override fun close() = Unit

        override fun timeout(): Timeout = Timeout.NONE
    }
}
