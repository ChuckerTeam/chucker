package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.api.config.ErrorsFeature
import com.chuckerteam.chucker.api.config.HttpFeature
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.FeatureManager
import com.chuckerteam.chucker.internal.support.NotificationHelper

/**
 * The collector responsible of collecting data from a [ChuckerInterceptor] and
 * storing it/displaying push notification. You need to instantiate one of those and
 * provide it to
 *
 * @param context An Android Context
 */
class ChuckerCollector(
    context: Context
) {
    private val retentionManager: RetentionManager = RetentionManager(context)
    private val notificationHelper: NotificationHelper = NotificationHelper(context)
    private val httpFeature: HttpFeature = FeatureManager.find()
    private val errorsFeature: ErrorsFeature = FeatureManager.find()

    init {
        RepositoryProvider.initialize(context)
    }

    /**
     * Call this method when a throwable is triggered and you want to record it.
     * @param tag A tag you choose
     * @param throwable The triggered [Throwable]
     */
    fun onError(tag: String, throwable: Throwable) {
        if (!errorsFeature.enabled) return

        val recordedThrowable = RecordedThrowable(tag, throwable)
        RepositoryProvider.throwable().saveThrowable(recordedThrowable)
        if (errorsFeature.showNotification) {
            notificationHelper.show(recordedThrowable)
        }
        retentionManager.doMaintenance()
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    internal fun onRequestSent(transaction: HttpTransaction) {
        if (!httpFeature.enabled) return

        RepositoryProvider.transaction().insertTransaction(transaction)
        if (httpFeature.showNotification) {
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
        if (!httpFeature.enabled) return

        val updated = RepositoryProvider.transaction().updateTransaction(transaction)
        if (httpFeature.showNotification && updated > 0) {
            notificationHelper.show(transaction)
        }
    }
}
