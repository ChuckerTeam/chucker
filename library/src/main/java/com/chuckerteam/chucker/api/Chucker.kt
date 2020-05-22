package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntDef
import com.chuckerteam.chucker.internal.support.ChuckerCrashHandler
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.ui.MainActivity

/**
 * Chucker methods and utilities to interact with the library.
 */
object Chucker {

    @Deprecated("This variable will be removed in 4.x release")
    const val SCREEN_HTTP = 1
    @Deprecated("This variable will be removed in 4.x release")
    const val SCREEN_ERROR = 2

    /**
     * Check if this instance is the operation one or no-op.
     * @return `true` if this is the operation instance.
     */
    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    val isOp = true

    /**
     * Get an Intent to launch the Chucker UI directly.
     * @param context An Android [Context].
     * @param screen The [Screen] to display: SCREEN_HTTP or SCREEN_ERROR.
     * @return An Intent for the main Chucker Activity that can be started with [Context.startActivity].
     */
    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith("Chucker.getLaunchIntent(context)"),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    fun getLaunchIntent(context: Context, @Screen screen: Int): Intent {
        return getLaunchIntent(context).putExtra(MainActivity.EXTRA_SCREEN, screen)
    }

    /**
     * Get an Intent to launch the Chucker UI directly.
     * @param context An Android [Context].
     * @return An Intent for the main Chucker Activity that can be started with [Context.startActivity].
     */
    @JvmStatic
    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Configure the default crash handler of the JVM to report all uncaught [Throwable] to Chucker.
     * You may only use it for debugging purpose.
     *
     * @param collector the ChuckerCollector
     */
    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith(""),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    fun registerDefaultCrashHandler(collector: ChuckerCollector) {
        Thread.setDefaultUncaughtExceptionHandler(ChuckerCrashHandler(collector))
    }

    /**
     * Method to dismiss the Chucker notification of HTTP Transactions
     */
    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith("Chucker.dismissNotifications(context)"),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    fun dismissTransactionsNotification(context: Context) {
        NotificationHelper(context).dismissTransactionsNotification()
    }

    /**
     * Method to dismiss the Chucker notification of Uncaught Errors.
     */
    @Deprecated(
        "This fun will be removed in 4.x release",
        ReplaceWith("Chucker.dismissNotifications(context)"),
        DeprecationLevel.WARNING
    )
    @JvmStatic
    fun dismissErrorsNotification(context: Context) {
        NotificationHelper(context).dismissErrorsNotification()
    }

    /**
     * Dismisses all previous Chucker notifications.
     */
    @JvmStatic
    fun dismissNotifications(context: Context) {
        NotificationHelper(context).dismissNotifications()
    }

    /**
     * Annotation used to specify which screen of Chucker should be launched.
     */
    @Deprecated("This param will be removed in 4.x release")
    @IntDef(value = [SCREEN_HTTP, SCREEN_ERROR])
    annotation class Screen

    internal const val LOG_TAG = "Chucker"
}
