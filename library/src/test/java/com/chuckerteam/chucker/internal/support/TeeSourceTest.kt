package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.SEGMENT_SIZE
import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.Sink
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.random.Random

internal class TeeSourceTest {
    @Test
    fun bytesReadFromUpstream_areAvailableDownstream() {
        val testSource = TestSource()

        val teeSource = TeeSource(testSource, sideStream = Buffer())
        val downstream = teeSource.buffer().use(BufferedSource::readByteString)

        assertThat(downstream).isEqualTo(testSource.content)
    }

    @Test
    fun bytesReadFromUpstream_areAvailableToSideChannel() {
        val testSource = TestSource()
        val sideStream = Buffer()

        val teeSource = TeeSource(testSource, sideStream)
        teeSource.buffer().use(BufferedSource::readByteString)

        assertThat(sideStream.snapshot()).isEqualTo(testSource.content)
    }

    @Test
    fun bytesPulledFromUpstream_arePulledToSideChannel_alongTheDownstream() {
        val repetitions = Random.nextInt(1, 100)
        val testSource = TestSource(repetitions * SEGMENT_SIZE.toInt())
        val sideStream = Buffer()

        val teeSource = TeeSource(testSource, sideStream)
        teeSource.buffer().use { source ->
            repeat(repetitions) { index ->
                source.readByteString(SEGMENT_SIZE)

                val subContent = testSource.content.substring(0, (index + 1) * SEGMENT_SIZE.toInt())
                assertThat(sideStream.snapshot()).isEqualTo(subContent)
            }
        }
    }

    @Test
    fun sideStreamThatFailsToWrite_doesNotFailDownstream() {
        val testSource = TestSource()

        val teeSource = TeeSource(testSource, sideStream = ThrowingSink(throwForWrite = true))
        val downstream = teeSource.buffer().use(BufferedSource::readByteString)

        assertThat(downstream).isEqualTo(testSource.content)
    }

    @Test
    fun sideStreamThatFailsToFlush_doesNotFailDownstream() {
        val testSource = TestSource()

        val teeSource = TeeSource(testSource, sideStream = ThrowingSink(throwForFlush = true))
        val downstream = teeSource.buffer().use(BufferedSource::readByteString)

        assertThat(downstream).isEqualTo(testSource.content)
    }

    @Test
    fun sideStreamThatFailsToClose_doesNotFailDownstream() {
        val testSource = TestSource()

        val teeSource = TeeSource(testSource, sideStream = ThrowingSink(throwForClose = true))
        val downstream = teeSource.buffer().use(BufferedSource::readByteString)

        assertThat(downstream).isEqualTo(testSource.content)
    }

    private class TestSource(contentLength: Int = 1_000) : Source {
        val content: ByteString = ByteString.of(*Random.nextBytes(contentLength))
        private val buffer = Buffer().apply { write(content) }

        override fun read(sink: Buffer, byteCount: Long): Long = buffer.read(sink, byteCount)

        override fun close() = buffer.close()

        override fun timeout(): Timeout = buffer.timeout()
    }

    private class ThrowingSink(
        private val throwForWrite: Boolean = false,
        private val throwForFlush: Boolean = false,
        private val throwForClose: Boolean = false,
    ) : Sink {
        override fun write(source: Buffer, byteCount: Long) {
            if (throwForWrite) throw IOException("Hello there!")
        }

        override fun flush() {
            if (throwForFlush) throw IOException("Hello there!")
        }

        override fun close() {
            if (throwForClose) throw IOException("Hello there!")
        }

        override fun timeout(): Timeout = Timeout.NONE
    }
}
