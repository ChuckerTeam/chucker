package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.ChuckerCollector

internal class ChuckerCrashHandler(private val collector: ChuckerCollector) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        collector.onError("Error caught on ${thread.name} thread", throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
