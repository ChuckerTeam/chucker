package com.chuckerteam.chucker.internal.support

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer
import okio.sink

public object FileSaver {
    public suspend fun saveFile(
        source: Source,
        uri: Uri,
        contentResolver: ContentResolver,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.sink().buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }.onFailure {
                Logger.error("Failed to save data to a file", it)
                return@withContext false
            }
            return@withContext true
        }
    }
}
