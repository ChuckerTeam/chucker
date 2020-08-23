package com.chuckerteam.chucker.internal.support

import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

internal object FileFactory {
    private val uniqueIdGenerator = AtomicLong()

    fun create(directory: File) = create(directory, fileName = "chucker-${uniqueIdGenerator.getAndIncrement()}")

    fun create(directory: File, fileName: String): File? = try {
        File(directory, fileName).apply {
            if (exists()) {
                delete()
            }
            parentFile?.mkdirs()
            if (!createNewFile()) {
                throw IOException("Failed to create a Chucker file: $this")
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
