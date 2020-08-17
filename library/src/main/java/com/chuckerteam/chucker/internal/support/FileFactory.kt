package com.chuckerteam.chucker.internal.support

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

internal class FileFactory(
    private val getFileDirectory: () -> File?
) {
    private val uniqueIdGenerator = AtomicLong()

    fun create() = create(filename = "chucker-${uniqueIdGenerator.getAndIncrement()}")

    fun create(filename: String): File? = try {
        val directory = getFileDirectory()
            ?: throw FileNotFoundException("Failed to create directory for temporary Chucker files")
        File(directory, filename).apply {
            if (exists()) {
                delete()
            }
            parentFile?.mkdirs()
            createNewFile()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
