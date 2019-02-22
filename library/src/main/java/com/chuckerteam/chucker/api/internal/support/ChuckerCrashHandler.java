package com.chuckerteam.chucker.api.internal.support;

import com.chuckerteam.chucker.api.ChuckerCollector;

/**
 * @author Olivier Perez
 */
public class ChuckerCrashHandler implements Thread.UncaughtExceptionHandler {

    private final ChuckerCollector collector;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public ChuckerCrashHandler(ChuckerCollector collector) {
        this.collector = collector;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        collector.onError("Error caught on " + t.getName() + " thread", e);
        defaultHandler.uncaughtException(t, e);
    }
}
