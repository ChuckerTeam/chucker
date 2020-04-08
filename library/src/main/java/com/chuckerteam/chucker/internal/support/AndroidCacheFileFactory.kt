package com.chuckerteam.chucker.internal.support

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicLong

internal class AndroidCacheFileFactory(
    context: Context
) : FileFactory {
    private val fileDir = context.cacheDir
    private val uniqueIdGenerator = AtomicLong()

    override fun createFileForResponseBody(): File = File(fileDir, "chucker-${uniqueIdGenerator.getAndIncrement()}")

    override fun createFileForExport(): File  = File(fileDir, "transactions.txt").apply {
        if (exists()) {
            delete()
        }
        createNewFile()
    }
}
