package com.chuckerteam.chucker.internal.support

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.chuckerteam.chucker.R

fun AppCompatActivity.shareError(errorText: String) {
    share(
        this,
        errorText,
        getString(R.string.chucker_share_error_title),
        getString(R.string.chucker_share_error_subject)
    )
}

fun AppCompatActivity.shareTransaction(transactionText: String) {
    share(
        this,
        transactionText,
        getString(R.string.chucker_share_transaction_title),
        getString(R.string.chucker_share_transaction_subject)
    )
}

private fun share(
    activity: AppCompatActivity,
    textToShare: String,
    shareTitle: String? = null,
    shareSubject: String? = null
) {
    activity.startActivity(
        ShareCompat.IntentBuilder.from(activity)
            .setType(MIME_TYPE)
            .setChooserTitle(shareTitle)
            .setSubject(shareSubject)
            .setText(textToShare)
            .createChooserIntent()
    )
}

private const val MIME_TYPE = "text/plain"
