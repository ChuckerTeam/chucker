package com.chuckerteam.chucker.sample.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri

/**
 * Opens the specified [url] in Google Chrome if it is installed on the device.
 * If Chrome is unavailable, this will fall back to the system's default browser
 * or display a chooser dialog, allowing the user to select from available browsers.
 *
 * @param context The [Context] used to start the browser activity.
 * @param url The web address to open, as a string. Must be a valid URL (e.g., https://example.com).
 *
 * Internally, this function:
 * 1. Constructs a Chrome-specific [Intent] targeting the com.android.chrome package.
 * 2. Creates a generic VIEW [Intent] as a fallback for any installed browser.
 * 3. Attempts to launch the Chrome-specific intent.
 * 4. On [ActivityNotFoundException], logs the failure and shows a chooser for the generic intent.
 */
internal fun openUrlInBrowser(
    context: Context,
    url: String,
) {
    // Prepare Chrome-specific intent
    val chromeIntent =
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setPackage("com.android.chrome")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    // Fallback generic intent
    val genericIntent =
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    try {
        context.startActivity(chromeIntent)
    } catch (e: ActivityNotFoundException) {
        Log.e(
            "openUrlInBrowser",
            "Chrome not found, falling back to default browser: ${e.message}",
            e,
        )
        context.startActivity(
            Intent.createChooser(genericIntent, "Open with"),
        )
    }
}
