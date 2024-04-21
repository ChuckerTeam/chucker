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
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(NoLoggerRule::class)
internal class FileSaverTest {
    @Test
    fun saveFile() =
        runTest {
            val source = TEST_FILE_CONTENT.byteInputStream().source()
            val uri =
                mockk<Uri> {
                    every { scheme } returns TEST_FILE_SCHEME
                    every { path } returns TEST_FILENAME
                }
            val contentResolver =
                mockk<ContentResolver> {
                    every { openOutputStream(any()) } returns uri.toFile().outputStream()
                }

            val file = uri.toFile()

            file.createNewFile()

            FileSaver.saveFile(source, uri, contentResolver)

            assertThat(file.length()).isEqualTo(TEST_FILE_CONTENT.length)
            assertThat(file.readText()).isEqualTo(TEST_FILE_CONTENT)

            file.delete()
        }

    private companion object {
        private const val TEST_FILENAME = "test_file"
        private const val TEST_FILE_SCHEME = "file"
        private const val TEST_FILE_CONTENT = "Hello world!"
    }
}
