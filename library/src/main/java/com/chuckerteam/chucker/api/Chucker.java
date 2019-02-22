/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuckerteam.chucker.api;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import com.chuckerteam.chucker.api.internal.support.ChuckerCrashHandler;
import com.chuckerteam.chucker.api.internal.support.NotificationHelper;
import com.chuckerteam.chucker.api.internal.ui.MainActivity;

/**
 * Chucker utilities.
 */
public class Chucker {

    public static final int SCREEN_HTTP = 1;
    public static final int SCREEN_ERROR = 2;

    /**
     * Get an Intent to launch the Chucker UI directly.
     *
     * @param context A Context.
     * @param screen The screen to display: SCREEN_HTTP or SCREEN_ERROR.
     * @return An Intent for the main Chucker Activity that can be started with {@link Context#startActivity(Intent)}.
     */
    public static Intent getLaunchIntent(Context context, @Screen int screen) {
        return new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.EXTRA_SCREEN, screen);
    }

    /**
     * Configure the default crash handler of the JVM to report all uncaught Throwable to Chucker.
     * You may only use it for debugging purpose.
     *
     * @param collector the ChuckerCollector
     */
    public static void registerDefaultCrashHanlder(final ChuckerCollector collector) {
        Thread.setDefaultUncaughtExceptionHandler(new ChuckerCrashHandler(collector));
    }

    public static void dismissTransactionsNotification(Context context) {
        new NotificationHelper(context).dismissTransactionsNotification();
    }

    public static void dismissErrorsNotification(Context context) {
        new NotificationHelper(context).dismissErrorsNotification();
    }

    /**
     * Check if this instance is the operation one or no-op.
     *
     * @return {@code true} if this is the operation instance.
     */
    public static boolean isOp() {
        return true;
    }

    @IntDef(value = {SCREEN_HTTP, SCREEN_ERROR})
    public @interface Screen {}
}