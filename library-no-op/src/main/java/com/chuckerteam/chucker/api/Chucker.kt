package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
object Chucker {

    const val SCREEN_HTTP = 1
    const val SCREEN_ERROR = 2

    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    val isOp = false

    @JvmStatic
    fun getLaunchIntent(context: Context, screen: Int): Intent {
        return Intent()
    }

    @JvmStatic
    fun registerDefaultCrashHandler(collector: ChuckerCollector) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    fun dismissTransactionsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    fun dismissErrorsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
