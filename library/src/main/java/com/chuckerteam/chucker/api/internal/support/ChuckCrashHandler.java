package com.chuckerteam.chucker.api.internal.support;

import com.chuckerteam.chucker.api.ChuckCollector;

/**
 * @author Olivier Perez
 */
public class ChuckCrashHandler implements Thread.UncaughtExceptionHandler {

    private final ChuckCollector collector;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public ChuckCrashHandler(ChuckCollector collector) {
        this.collector = collector;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        collector.onError("Error caught on " + t.getName() + " thread", e);
        defaultHandler.uncaughtException(t, e);
    }
}
