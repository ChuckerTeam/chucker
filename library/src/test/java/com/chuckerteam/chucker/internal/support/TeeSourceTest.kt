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
    private val teeCallback = TestTeeCallback()

    @Test
    fun bytesReadFromUpstream_areAvailableDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, teeCallback)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(downstream.snapshot()).isEqualTo(testSource.content)
    }

    @Test
    fun bytesReadFromUpstream_areAvailableToSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, teeCallback)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(teeCallback.fileContent).isEqualTo(testSource.content)
    }

    @Test
    fun bytesPulledFromUpstream_arePulledToSideChannel_alongTheDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val repetitions = Random.nextInt(1, 100)
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * repetitions)

        val teeSource = TeeSource(testSource, testFile, teeCallback)
        Okio.buffer(teeSource).use { source ->
            repeat(repetitions) { index ->
                source.readByteString(8_192)

                val subContent = testSource.content.substring(0, (index + 1) * 8_192)
                Okio.buffer(Okio.source(testFile)).use {
                    assertThat(it.readByteString()).isEqualTo(subContent)
                }
            }
        }
    }

    @Test
    fun tooBigSources_informOfFailures_inSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, teeCallback, readBytesLimit = 9_999)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(teeCallback.exception)
            .hasMessageThat()
            .isEqualTo("Capacity of 9999 bytes exceeded")
    }

    @Test
    fun tooBigSources_doNotResultInSuccess_ifTheUpstreamIsExhausted(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, teeCallback, readBytesLimit = 9_999)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(teeCallback.isSuccess).isFalse()
    }

    @Test
    fun tooBigSources_areAvailableDownstream(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val downstream = Buffer()

        val teeSource = TeeSource(testSource, testFile, teeCallback, readBytesLimit = 9_999)
        Okio.buffer(teeSource).use { it.readAll(downstream) }

        assertThat(downstream.snapshot()).isEqualTo(testSource.content)
    }

    @Test
    fun readException_informOfFailures_inSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = ThrowingSource

        val teeSource = TeeSource(testSource, testFile, teeCallback)

        assertThrows<IOException> {
            Okio.buffer(teeSource).use { it.readByte() }
        }

        assertThat(teeCallback.exception)
            .hasMessageThat()
            .isEqualTo("Hello there!")
    }

    @Test
    fun notConsumedUpstream_isNotConsideredSuccess(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * 2)

        val teeSource = TeeSource(testSource, testFile, teeCallback)
        Okio.buffer(teeSource).use { source ->
            source.readByteString(8_192)
        }

        assertThat(teeCallback.exception)
            .hasMessageThat()
            .isEqualTo("Upstream was not fully consumed")
    }

    @Test
    fun partiallyReadBytesFromUpstream_areAvailableToSideChannel(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * 2)

        val teeSource = TeeSource(testSource, testFile, teeCallback)
        Okio.buffer(teeSource).use { source ->
            source.readByteString(8_192)
        }

        assertThat(teeCallback.fileContent).isEqualTo(testSource.content.substring(0, 8_192))
    }

    private class TestSource(contentLength: Int = 1_000) : Source {
        val content: ByteString = ByteString.of(*Random.nextBytes(contentLength))
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

    private class TestTeeCallback : TeeSource.Callback {
        private var file: File? = null
        val fileContent get() = file?.let { Okio.buffer(Okio.source(it)).readByteString() }
        var exception: IOException? = null
        var isSuccess = false
            private set

        override fun onSuccess(file: File) {
            isSuccess = true
            this.file = file
        }

        override fun onFailure(exception: IOException, file: File) {
            this.exception = exception
            this.file = file
        }
    }
}
