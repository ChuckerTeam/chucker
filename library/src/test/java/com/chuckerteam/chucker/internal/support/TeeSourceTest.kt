package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.io.IOException
import kotlin.random.Random
import okio.Buffer
import okio.ByteString
import okio.Okio
import okio.Source
import okio.Timeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class TeeSourceTest {
    @Test
    fun teeInitialization_writesOkPrefix(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()

        TeeSource(testSource, testFile)

        Okio.buffer(Okio.source(testFile)).use {
            assertThat(it.readUtf8Line()).isEqualTo(TeeSource.PREFIX_OK)
            assertThat(it.exhausted()).isTrue()
        }
    }

    @Test
    fun bytesReadFromUpstream_areAvailableDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(downstream.snapshot()).isEqualTo(testSource.content)
    }

    @Test
    fun bytesReadFromUpstream_areAvailableToSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        Okio.buffer(Okio.source(testFile)).use {
            assertThat(it.readUtf8Line()).isEqualTo(TeeSource.PREFIX_OK)
            assertThat(it.readByteString()).isEqualTo(testSource.content)
            assertThat(it.exhausted()).isTrue()
        }
    }

    @Test
    fun bytesPulledFromUpstream_arePulledToSideChannel_alongTheDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val repetitions = Random.nextInt(0, 100)
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * repetitions)

        val teeSource = TeeSource(testSource, testFile)
        Okio.buffer(teeSource).use { source ->
            repeat(repetitions) { index ->
                source.readByteString(8_192)

                val subContent = testSource.content.substring(0, (index + 1) * 8_192)
                Okio.buffer(Okio.source(testFile)).use {
                    assertThat(it.readUtf8Line()).isEqualTo(TeeSource.PREFIX_OK)
                    assertThat(it.readByteString()).isEqualTo(subContent)
                }
            }
        }
    }

    @Test
    fun tooBigSources_areReplacedWithFailureHeader_inSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, readBytesLimit = 9_999)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        Okio.buffer(Okio.source(testFile)).use {
            assertThat(it.readUtf8Line()).isEqualTo(TeeSource.PREFIX_FAILURE)
            assertThat(it.exhausted()).isTrue()
        }
    }

    @Test
    fun tooBigSources_areAvailableDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(downstream.snapshot()).isEqualTo(testSource.content)
    }

    @Test
    fun readException_isReflectedInSideChannel_withFailureHeader(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = ThrowingSource

        val teeSource = TeeSource(testSource, testFile)

        assertThrows<IOException> {
            Okio.buffer(teeSource).use { it.readByte() }
        }

        Okio.buffer(Okio.source(testFile)).use {
            assertThat(it.readUtf8Line()).isEqualTo(TeeSource.PREFIX_FAILURE)
            assertThat(it.exhausted()).isTrue()
        }
    }

    private class TestSource(contentLength: Int = 1_000) : Source {
        val content = ByteString.of(*Random.nextBytes(contentLength))
        private val buffer = Buffer().apply { write(content) }

        override fun read(sink: Buffer, byteCount: Long): Long = buffer.read(sink, byteCount)

        override fun close() = buffer.close()

        override fun timeout(): Timeout = buffer.timeout()
    }

    private object ThrowingSource : Source {
        override fun read(sink: Buffer, byteCount: Long): Long {
            throw IOException("Hello there!")
        }

        override fun close() = Unit

        override fun timeout(): Timeout = Timeout.NONE
    }
}
