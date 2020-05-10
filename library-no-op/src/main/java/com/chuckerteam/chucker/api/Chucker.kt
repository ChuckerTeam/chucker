package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
object Chucker {

    @Deprecated("This variable will be removed in 4.x release")
    const val SCREEN_HTTP = 1
    @Deprecated("This variable will be removed in 4.x release")
    const val SCREEN_ERROR = 2

    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    val isOp = false

    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith("Chucker.getLaunchIntent(context)"),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    fun getLaunchIntent(context: Context, screen: Int): Intent = Intent()

    @JvmStatic
    fun getLaunchIntent(context: Context): Intent = Intent()

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
