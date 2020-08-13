package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidCacheFileFactoryTest {

    private val mockContext = mockk<Context>()

    @TempDir
    lateinit var tempDir: File

    @Test
    fun createsCacheFileParents() {
        every { mockContext.cacheDir } returns tempDir
        val androidCacheFileFactory = AndroidCacheFileFactory(mockContext)
        tempDir.deleteRecursively()
        assertThat(androidCacheFileFactory.create().isFile).isTrue()
    }
}
