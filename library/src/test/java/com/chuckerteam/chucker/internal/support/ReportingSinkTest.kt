package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.ByteString
import okio.Okio
import okio.Source
import okio.Timeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random

class ReportingSinkTest {
    private val reportingCallback = TestReportingCallback()

    @Test
    fun bytesWrittenToDownstream_areAvailableForConsumer(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource()
        val reportingSink = ReportingSink(testFile, reportingCallback)

        Okio.buffer(reportingSink).use { it.writeAll(testSource) }

        assertThat(reportingCallback.fileContent).isEqualTo(testSource.content)
    }

    @Test
    fun bytesWrittenToDownstream_canByLimited(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val reportingSink = ReportingSink(testFile, reportingCallback, writeBytesLimit = 9_999)

        Okio.buffer(reportingSink).use { it.writeAll(testSource) }

        val expectedContent = testSource.content.substring(0, 9_999)
        assertThat(reportingCallback.fileContent).isEqualTo(expectedContent)
    }

    @Test
    fun partiallyConsumedUpstream_hasReadBytesAvailableForConsumer(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * 2)
        val reportingSink = ReportingSink(testFile, reportingCallback)

        Okio.buffer(reportingSink).use { it.write(testSource, 8_192) }

        val expectedContent = testSource.content.substring(0, 8_192)
        assertThat(reportingCallback.fileContent).isEqualTo(expectedContent)
    }

    @Test
    fun exactlyWrittenBytesToDownstream_areAvailableForConsumer(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val repetitions = Random.nextInt(1, 100)
        // Okio uses 8KiB as a single size read.
        val testSource = TestSource(8_192 * repetitions)
        val reportingSink = ReportingSink(testFile, reportingCallback)

        Okio.buffer(reportingSink).use { sink ->
            repeat(repetitions) { sink.write(testSource, 8_192) }
        }

        assertThat(reportingCallback.fileContent).isEqualTo(testSource.content)
    }

    @Test
    fun exceptionWhileCreatingDownstream_areAvailableForConsumer(@TempDir tempDir: File) {
        assertThat(tempDir.deleteRecursively()).isTrue()

        val testFile = File(tempDir, "testFile")

        ReportingSink(testFile, reportingCallback)

        assertThat(reportingCallback.exception).apply {
            isInstanceOf(IOException::class.java)
            hasMessageThat().isEqualTo("Failed to use file $testFile by Chucker")
            hasCauseThat().isInstanceOf(FileNotFoundException::class.java)
        }
    }

    private class TestSource(contentLength: Int = 1_000) : Source {
        val content: ByteString = ByteString.of(*Random.nextBytes(contentLength))
        private val buffer = Buffer().apply { write(content) }

        override fun read(sink: Buffer, byteCount: Long): Long = buffer.read(sink, byteCount)

        override fun close() = buffer.close()

        override fun timeout(): Timeout = buffer.timeout()
    }

    private class TestReportingCallback : ReportingSink.Callback {
        private var file: File? = null
        val fileContent get() = file?.let { Okio.buffer(Okio.source(it)).readByteString() }
        var exception: IOException? = null
        var isSuccess = false
            private set

        override fun onClosed(file: File?, writtenBytesCount: Long) {
            isSuccess = true
            this.file = file
        }

        override fun onFailure(file: File?, exception: IOException) {
            this.exception = exception
            this.file = file
        }
    }
}
