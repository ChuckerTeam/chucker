package com.chuckerteam.chucker.internal.support

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.sink

internal interface Sharable {
    fun toSharableContent(context: Context): Source
}

internal fun Sharable.toSharableUtf8Content(
    context: Context,
) = toSharableContent(context).buffer().use(BufferedSource::readUtf8)

internal suspend fun Sharable.shareAsUtf8Text(
    activity: Activity,
    intentTitle: String,
    intentSubject: String,
): Intent {
    val content = withContext(Dispatchers.Default) { toSharableUtf8Content(activity) }
    return ShareCompat.IntentBuilder(activity)
        .setType("text/plain")
        .setChooserTitle(intentTitle)
        .setSubject(intentSubject)
        .setText(content)
        .createChooserIntent()
}

internal fun Sharable.writeToFile(
    context: Context,
    fileName: String,
): Uri? {
    val cache = context.cacheDir
    if (cache == null) {
        Logger.warn("Failed to obtain a valid cache directory for file export")
        return null
    }

    val file = FileFactory.create(cache, fileName)
    if (file == null) {
        Logger.warn("Failed to create an export file")
        return null
    }

    val fileContent = toSharableContent(context)
    file.sink().buffer().use { it.writeAll(fileContent) }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.com.chuckerteam.chucker.provider",
        file
    )
}

internal fun Sharable.shareAsFile(
    activity: Activity,
    fileName: String,
    intentTitle: String,
    intentSubject: String,
    clipDataLabel: String,
): Intent? {
    val uri = writeToFile(activity, fileName) ?: return null
    val shareIntent = ShareCompat.IntentBuilder(activity)
        .setType(activity.contentResolver.getType(uri))
        .setChooserTitle(intentTitle)
        .setSubject(intentSubject)
        .setStream(uri)
        .intent
    shareIntent.apply {
        clipData = ClipData.newRawUri(clipDataLabel, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(shareIntent, intentTitle)
}
