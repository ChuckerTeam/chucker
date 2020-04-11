package com.chuckerteam.chucker.internal.support

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicLong

internal class AndroidCacheFileFactory(
    context: Context
) : FileFactory {
    private val fileDir = context.cacheDir
    private val uniqueIdGenerator = AtomicLong()

    override fun create(filename: String): File {
        val file = if (filename.isEmpty()) {
            File(fileDir, "chucker-${uniqueIdGenerator.getAndIncrement()}")
        } else {
            File(fileDir, filename)
        }
        return file.apply {
            if (exists()) {
                delete()
            }
            createNewFile()
        }
    }
}
