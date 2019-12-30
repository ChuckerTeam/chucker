package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
object Chucker {

    const val SCREEN_HTTP = 1
    const val SCREEN_ERROR = 2

    val isOp = false

    fun getLaunchIntent(context: Context, screen: Int): Intent {
        return Intent()
    }

    fun registerDefaultCrashHandler(collector: ChuckerCollector) {
        // Empty method for the library-no-op artifact
    }

    fun dismissTransactionsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }

    fun dismissErrorsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
