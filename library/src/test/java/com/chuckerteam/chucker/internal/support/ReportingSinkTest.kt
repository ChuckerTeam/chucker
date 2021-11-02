package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.util.SEGMENT_SIZE
import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.ByteString
import okio.Source
import okio.Timeout
import okio.buffer
import okio.source
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random

internal class ReportingSinkTest {
    private val reportingCallback = TestReportingCallback()

    @Test
    fun `downstream bytes are reported`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val repetitions = Random.nextInt(1, 100)
        val testSource = TestSource(repetitions * SEGMENT_SIZE.toInt())
        val reportingSink = ReportingSink(testFile, reportingCallback)

        reportingSink.buffer().use { it.writeAll(testSource) }

        assertThat(reportingCallback.fileContent).isEqualTo(testSource.content)
    }

    @Test
    fun `downstream bytes can be limited`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(10_000)
        val reportingSink = ReportingSink(testFile, reportingCallback, writeByteLimit = 9_999)

        reportingSink.buffer().use { it.writeAll(testSource) }

        val expectedContent = testSource.content.substring(0, 9_999)
        assertThat(reportingCallback.fileContent).isEqualTo(expectedContent)
    }

    @Test
    fun `partially consumed upstream bytes are reported`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "testFile")
        val testSource = TestSource(2 * SEGMENT_SIZE.toInt())
        val reportingSink = ReportingSink(testFile, reportingCallback)

        reportingSink.buffer().use { it.write(testSource, SEGMENT_SIZE) }

        val expectedContent = testSource.content.substring(0, SEGMENT_SIZE.toInt())
        assertThat(reportingCallback.fileContent).isEqualTo(expectedContent)
    }

    @Test
    fun `exception while creating downstream is reported`(@TempDir tempDir: File) {
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
        private val buffer = Buffer().write(content)

        override fun read(sink: Buffer, byteCount: Long): Long = buffer.read(sink, byteCount)

        override fun close() = buffer.close()

        override fun timeout(): Timeout = buffer.timeout()
    }

    private class TestReportingCallback : ReportingSink.Callback {
        private var file: File? = null
        val fileContent get() = file?.source()?.buffer()?.readByteString()
        var exception: IOException? = null
        var isSuccess = false
            private set

        override fun onClosed(file: File?, sourceByteCount: Long) {
            isSuccess = true
            this.file = file
        }

        override fun onFailure(file: File?, exception: IOException) {
            this.exception = exception
            this.file = file
        }
    }
}
