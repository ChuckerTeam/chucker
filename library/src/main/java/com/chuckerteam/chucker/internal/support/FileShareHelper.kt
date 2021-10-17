package com.chuckerteam.chucker.internal.support

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.chuckerteam.chucker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal const val HAR_EXPORT_FILENAME = "transactions.har"
internal const val TXT_EXPORT_FILENAME = "transactions.txt"

internal object FileShareHelper {
    suspend fun share(activity: Activity, exportFilename: String, fileContentsFactory: suspend () -> String) {
        val cache = activity.cacheDir
        if (cache == null) {
            Logger.error("Failed to obtain a valid cache directory for Chucker file export")
            Toast.makeText(activity, R.string.chucker_export_no_file, Toast.LENGTH_SHORT).show()
            return
        }

        val file = createExportFile(cache, exportFilename, fileContentsFactory())

        if (file == null) {
            Logger.error("Failed to create an export file for Chucker")
            Toast.makeText(activity, R.string.chucker_export_no_file, Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.com.chuckerteam.chucker.provider",
            file
        )
        shareFile(activity, uri)
    }

    private suspend fun createExportFile(
        cacheDirectory: File,
        exportFilename: String,
        content: String,
    ): File? = withContext(Dispatchers.IO) {
        FileFactory.create(cacheDirectory, exportFilename)?.apply { writeText(content) }
    }

    private fun shareFile(activity: Activity, uri: Uri) {
        val sendIntent = ShareCompat.IntentBuilder.from(activity)
            .setType(activity.contentResolver.getType(uri))
            .setChooserTitle(activity.getString(R.string.chucker_share_all_transactions_title))
            .setSubject(activity.getString(R.string.chucker_share_all_transactions_subject))
            .setStream(uri)
            .intent

        sendIntent.apply {
            clipData = ClipData.newRawUri("transactions", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(
            Intent.createChooser(
                sendIntent,
                activity.getString(R.string.chucker_share_all_transactions_title)
            )
        )
    }
}
