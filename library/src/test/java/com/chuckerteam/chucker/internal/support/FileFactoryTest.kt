package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileFactoryTest {
    @TempDir lateinit var tempDir: File

    @Test
    fun fileIsCreated_evenIfParentDirectory_isDeleted() {
        val fileFactory = FileFactory { tempDir }

        assertThat(tempDir.deleteRecursively()).isTrue()
        assertThat(fileFactory.create()!!.isFile).isTrue()
    }
}
