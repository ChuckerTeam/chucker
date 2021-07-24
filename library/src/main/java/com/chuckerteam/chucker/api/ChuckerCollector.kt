package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.GroupSingleton
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * The collector responsible of collecting data from a [ChuckerInterceptor] and
 * storing it/displaying push notification. You need to instantiate one of those and
 * provide it to
 *
 * @param context An Android Context
 * @param showNotification Control whether a notification is shown while HTTP activity
 * is recorded.
 * @param retentionPeriod Set the retention period for HTTP transaction data captured
 * by this collector. The default is one week.
 */
public class ChuckerCollector @JvmOverloads constructor(
    context: Context,
    public var showNotification: Boolean = true,
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK,
    groups: List<Group> = listOf()
) {
    private val retentionManager: RetentionManager = RetentionManager(context, retentionPeriod)
    private val notificationHelper: NotificationHelper = NotificationHelper(context)
    private val scope = MainScope()

    init {
        RepositoryProvider.initialize(context)
        GroupSingleton.groups = groups.toMutableList()
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    internal fun onRequestSent(transaction: HttpTransaction) {
        scope.launch {
            RepositoryProvider.transaction().insertTransaction(transaction)

            if (showNotification) {
                notificationHelper.show(transaction)
            }
            retentionManager.doMaintenance()
        }
    }

    /**
     * Call this method when you received the response of an HTTP request.
     * It must be called after [ChuckerCollector.onRequestSent].
     * @param transaction The sent HTTP transaction completed with the response
     */
    internal fun onResponseReceived(transaction: HttpTransaction) {
        scope.launch {
            val updated = RepositoryProvider.transaction().updateTransaction(transaction)
            if (showNotification && updated > 0) {
                notificationHelper.show(transaction)
            }
        }
    }
}
