package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember")
public object Chucker {

    @Deprecated("This variable will be removed in 4.x release")
    public const val SCREEN_HTTP: Int = 1

    @Deprecated("This variable will be removed in 4.x release")
    public const val SCREEN_ERROR: Int = 2

    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    public val isOp: Boolean = false

    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith("Chucker.getLaunchIntent(context)"),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    public fun getLaunchIntent(context: Context, screen: Int): Intent = Intent()

    @JvmStatic
    public fun getLaunchIntent(context: Context): Intent = Intent()

    @JvmStatic
    public fun registerDefaultCrashHandler(collector: ChuckerCollector) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    public fun dismissTransactionsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    public fun dismissErrorsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
