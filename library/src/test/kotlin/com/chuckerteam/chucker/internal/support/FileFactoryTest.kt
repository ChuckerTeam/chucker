package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileFactoryTest {
    @TempDir lateinit var tempDir: File

    @Test
    fun `file is created if parent does not exist`() {
        assertThat(tempDir.deleteRecursively()).isTrue()

        assertThat(FileFactory.create(tempDir)!!.isFile).isTrue()
    }
}
