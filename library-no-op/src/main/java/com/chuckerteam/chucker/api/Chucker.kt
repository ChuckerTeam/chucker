package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
object Chucker {

    val SCREEN_HTTP = 1
    val SCREEN_ERROR = 2

    val isOp = false

    @JvmStatic fun getLaunchIntent(context: Context?, screen: Int): Intent {
        return Intent()
    }

    @JvmStatic fun registerDefaultCrashHanlder(collector: ChuckerCollector) {}

    @JvmStatic fun dismissTransactionsNotification(context: Context) {}

    @JvmStatic fun dismissErrorsNotification(context: Context) {}

}
