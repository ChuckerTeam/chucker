package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.content.Intent

fun Context.share(
    content: String,
    title: String? = null,
    subject: String? = null
) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        title?.let { putExtra(Intent.EXTRA_TITLE, it) }
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(sendIntent, null))
}
