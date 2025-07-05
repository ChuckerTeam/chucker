package com.chuckerteam.chucker.sample.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri

/**
 * Opens the specified [url] in the user's preferred browser.
 *
 * @param context The [Context] used to start the browser activity.
 * @param url The web address to open.
 */
internal fun openUrlInBrowser(
    context: Context,
    url: String,
) {
    val intent =
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Log.e("openUrlInBrowser", "No application can handle this request: ${e.message}", e)
    }
}
