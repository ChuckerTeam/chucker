package com.chuckerteam.chucker.internal.support

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer
import okio.sink

/**
 * Utility class to save a file from a [Source] to a [Uri].
 */
public object FileSaver {
    /**
     * Saves the data from the [source] to the file at the [uri] using the [contentResolver].
     *
     * @param source The source of the data to save.
     * @param uri The URI of the file to save the data to.
     * @param contentResolver The content resolver to use to save the data.
     * @return `true` if the data was saved successfully, `false` otherwise.
     */
    public suspend fun saveFile(
        source: Source,
        uri: Uri,
        contentResolver: ContentResolver,
    ): Boolean =
        withContext(Dispatchers.IO) {
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
