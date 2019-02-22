package com.chuckerteam.chucker.api;

import android.content.Context;
import android.net.Uri;

/**
 * @author Olivier Perez
 */
public class ChuckerCollector {

    private final Context context;

    public ChuckerCollector(Context context) {
        this.context = context;
    }

    public Uri onRequestSent(Object object) {
        return null;
    }

    public int onResponseReceived(Object object, Object object2) {
        return 0;
    }

    public void onError(Object object, Object object2) {
    }

    public ChuckerCollector showNotification(boolean showNotification) {
        return this;
    }

    public ChuckerCollector retentionManager(Object object) {
        return this;
    }

    public enum Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,
        /**
         * Retain data for the last day.
         */
        ONE_DAY,
        /**
         * Retain data for the last week.
         */
        ONE_WEEK,
        /**
         * Retain data forever.
         */
        FOREVER
    }

}
