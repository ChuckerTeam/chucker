package com.chuckerteam.chucker.internal.support

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicLong

internal class AndroidCacheFileFactory(
    context: Context
) : FileFactory {
    private val fileDir = context.cacheDir
    private val uniqueIdGenerator = AtomicLong()

    override fun create(): File = File(fileDir, "chucker-${uniqueIdGenerator.getAndIncrement()}")

    override fun createExportFile(): File {
        val file = File(fileDir, "transactions")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }
}
