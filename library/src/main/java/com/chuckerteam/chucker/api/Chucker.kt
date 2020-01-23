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

    const val SCREEN_HTTP = 1
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
    @JvmStatic
    fun getLaunchIntent(context: Context, @Screen screen: Int): Intent {
        return Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(MainActivity.EXTRA_SCREEN, screen)
    }

    /**
     * Configure the default crash handler of the JVM to report all uncaught [Throwable] to Chucker.
     * You may only use it for debugging purpose.
     *
     * @param collector the ChuckerCollector
     */
    @JvmStatic
    fun registerDefaultCrashHandler(collector: ChuckerCollector) {
        Thread.setDefaultUncaughtExceptionHandler(ChuckerCrashHandler(collector))
    }

    /**
     * Method to dismiss the Chucker notification of HTTP Transactions
     */
    @JvmStatic
    fun dismissTransactionsNotification(context: Context) {
        NotificationHelper(context).dismissTransactionsNotification()
    }

    /**
     * Method to dismiss the Chucker notification of Uncaught Errors.
     */
    @JvmStatic
    fun dismissErrorsNotification(context: Context) {
        NotificationHelper(context).dismissErrorsNotification()
    }

    /**
     * Annotation used to specify which screen of Chucker should be launched.
     */
    @IntDef(value = [SCREEN_HTTP, SCREEN_ERROR])
    annotation class Screen

    internal const val LOG_TAG = "Chucker"
}
