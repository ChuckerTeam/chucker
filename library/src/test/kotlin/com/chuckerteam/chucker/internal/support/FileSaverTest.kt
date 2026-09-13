package com.chuckerteam.chucker.internal.support

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toFile
import com.chuckerteam.chucker.util.NoLoggerRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.source
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.rules.TemporaryFolder

@ExtendWith(NoLoggerRule::class)
internal class FileSaverTest {
    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    init {
        temporaryFolder.create()
    }

    private val file = temporaryFolder.newFile(TEST_FILENAME)

    private val uri =
        mockk<Uri> {
            every { scheme } returns TEST_FILE_SCHEME
            every { path } returns file.path
        }
    private val contentResolver =
        mockk<ContentResolver> {
            every { openOutputStream(any()) } returns uri.toFile().outputStream()
        }

    @Test
    fun `normal content test`() =
        runTest {
            val source = TEST_FILE_CONTENT.byteInputStream().source()

            FileSaver.saveFile(source, uri, contentResolver)

            assertThat(file.length()).isEqualTo(TEST_FILE_CONTENT.length)
            assertThat(file.readText()).isEqualTo(TEST_FILE_CONTENT)
        }

    @Test
    fun `empty content test`() =
        runTest {
            val source = TEST_EMPTY_FILE_CONTENT.byteInputStream().source()

            FileSaver.saveFile(source, uri, contentResolver)

            assertThat(file.length()).isEqualTo(TEST_EMPTY_FILE_CONTENT.length)
            assertThat(file.readText()).isEqualTo(TEST_EMPTY_FILE_CONTENT)
        }

    private companion object {
        private const val TEST_FILENAME = "test_file"
        private const val TEST_FILE_SCHEME = "file"
        private const val TEST_FILE_CONTENT = "Hello world!"
        private const val TEST_EMPTY_FILE_CONTENT = ""
    }
}
