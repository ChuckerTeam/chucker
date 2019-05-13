package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.api.internal.support.NotificationHelper

/**
 * The collector responsible of collecting data from a [ChuckerInterceptor] and
 * storing it/displaying push notification. You need to instantiate one of those and
 * provide it to
 *
 * @param context An Android Context
 * @param showNotification Control whether a notification is shown while HTTP activity
 * is recorded.
 * @param retentionManager Set the retention period for HTTP transaction data captured
 * by this collector. The default is one week.
 */
class ChuckerCollector @JvmOverloads constructor(
        context: Context,
        var showNotification: Boolean = true,
        var retentionManager: RetentionManager = RetentionManager(context)
) {

    private val notificationHelper: NotificationHelper = NotificationHelper(context)

    init {
        RepositoryProvider.initialize(context)
    }

    /**
     * Call this method when a throwable is triggered and you want to record it.
     * @param tag       A tag you choose
     * @param throwable The triggered [Throwable]
     */
    fun onError(tag: String, throwable: Throwable) {
        val recordedThrowable = RecordedThrowable(tag, throwable)
        RepositoryProvider.throwable().saveThrowable(recordedThrowable)
        if (showNotification) {
            notificationHelper.show(recordedThrowable)
        }
        retentionManager.doMaintenance()
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    internal fun onRequestSent(transaction: HttpTransaction) {
        RepositoryProvider.transaction().insertTransaction(transaction)
        if (showNotification) {
            notificationHelper.show(transaction)
        }
        retentionManager.doMaintenance()
    }

    /**
     * Call this method when you received the response of an HTTP request.
     * It must be called after [ChuckerCollector.onRequestSent].
     * @param transaction The sent HTTP transaction completed with the response
     */
    internal fun onResponseReceived(transaction: HttpTransaction) {
        val updated = RepositoryProvider.transaction().updateTransaction(transaction)
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction)
        }
    }
}
