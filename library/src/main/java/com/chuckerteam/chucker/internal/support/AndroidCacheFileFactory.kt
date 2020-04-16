package com.chuckerteam.chucker.internal.support

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicLong

internal const val EXPORT_FILENAME = "transactions.txt"

internal class AndroidCacheFileFactory(
    context: Context
) : FileFactory {
    private val fileDir = context.cacheDir
    private val uniqueIdGenerator = AtomicLong()

    override fun create() = create(filename = "chucker-${uniqueIdGenerator.getAndIncrement()}")

    override fun create(filename: String): File = File(fileDir, filename).apply {
        if (exists()) {
            delete()
        }
        createNewFile()
    }
}
