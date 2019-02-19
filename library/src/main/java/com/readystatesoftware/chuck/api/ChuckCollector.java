package com.readystatesoftware.chuck.api;

import android.content.Context;

import com.readystatesoftware.chuck.internal.data.repository.RepositoryProvider;
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;

public class ChuckCollector {

    private static final Period DEFAULT_RETENTION = Period.ONE_WEEK;

    private final NotificationHelper notificationHelper;
    private RetentionManager retentionManager;

    private boolean showNotification;

    public ChuckCollector(Context context) {
        notificationHelper = new NotificationHelper(context);
        showNotification = true;
        RepositoryProvider.initialize(context);
        retentionManager = new RetentionManager(context, DEFAULT_RETENTION);
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    public void onRequestSent(HttpTransaction transaction) {
        RepositoryProvider.transaction().insertTransaction(transaction);
        if (showNotification) {
            notificationHelper.show(transaction);
        }
        retentionManager.doMaintenance();
    }

    /**
     * Call this method when you received the response of an HTTP request.
     * It must be called after {@link ChuckCollector#onRequestSent}.
     * @param transaction The sent HTTP transaction completed with the response
     */
    public void onResponseReceived(HttpTransaction transaction) {
        int updated = RepositoryProvider.transaction().updateTransaction(transaction);
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction);
        }
    }

    /**
     * Call this method when a throwable is triggered and you want to record it.
     * @param tag       A tag you choose
     * @param throwable The triggered throwable
     */
    public void onError(String tag, Throwable throwable) {
        RecordedThrowable recordedThrowable = new RecordedThrowable(tag, throwable);
        RepositoryProvider.throwable().saveThrowable(recordedThrowable);
        if (showNotification) {
            notificationHelper.show(recordedThrowable);
        }
        retentionManager.doMaintenance();
    }

    /**
     * Control whether a notification is shown while HTTP activity is recorded.
     *
     * @param showNotification true to show a notification, false to suppress it.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckCollector showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    /**
     * Set the retention period for HTTP transaction data captured by this interceptor.
     * The default is one week.
     *
     * @param retentionManager the manager of retention of stored transactions and errors.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckCollector retentionManager(RetentionManager retentionManager) {
        this.retentionManager = retentionManager;
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
