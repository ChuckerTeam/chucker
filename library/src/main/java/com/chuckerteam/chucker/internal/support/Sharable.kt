package com.chuckerteam.chucker.internal.support

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.Logger.warn
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
    return ShareCompat.IntentBuilder.from(activity)
        .setType("text/plain")
        .setChooserTitle(intentTitle)
        .setSubject(intentSubject)
        .setText(content)
        .createChooserIntent()
}

internal suspend fun Sharable.shareAsFile(
    activity: Activity,
    fileName: String,
    intentTitle: String,
    intentSubject: String,
    clipDataLabel: String,
): Intent? {
    val cache = activity.cacheDir
    if (cache == null) {
        warn("Failed to obtain a valid cache directory for Chucker file export")
        Toast.makeText(activity, R.string.chucker_export_no_file, Toast.LENGTH_SHORT).show()
        return null
    }

    val file = FileFactory.create(cache, fileName)
    if (file == null) {
        warn("Failed to create an export file for Chucker")
        Toast.makeText(activity, R.string.chucker_export_no_file, Toast.LENGTH_SHORT).show()
        return null
    }

    val fileContent = withContext(Dispatchers.Default) { toSharableContent(activity) }
    withContext(Dispatchers.IO) {
        file.sink().buffer().use { it.writeAll(fileContent) }
    }

    val uri = FileProvider.getUriForFile(
        activity,
        "${activity.packageName}.com.chuckerteam.chucker.provider",
        file
    )
    val shareIntent = ShareCompat.IntentBuilder.from(activity)
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
