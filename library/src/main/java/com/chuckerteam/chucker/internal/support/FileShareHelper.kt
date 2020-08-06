package com.chuckerteam.chucker.internal.support

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.chuckerteam.chucker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class FileShareHelper(
    private val activity: Activity,
    private val exportFilename: String,
    private val fileContentsFactory: suspend () -> String
) {
    private val cacheFileFactory: FileFactory by lazy {
        AndroidCacheFileFactory(activity)
    }

    suspend fun share() {
        val file = createExportFile(fileContentsFactory())
        val uri = FileProvider.getUriForFile(
            activity,
            activity.getString(R.string.chucker_provider_authority),
            file
        )
        shareFile(uri)
    }

    private suspend fun createExportFile(content: String): File = withContext(Dispatchers.IO) {
        val file = cacheFileFactory.create(exportFilename)
        file.writeText(content)
        return@withContext file
    }

    private fun shareFile(uri: Uri) {
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

        activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.chucker_share_all_transactions_title)))
    }
}
