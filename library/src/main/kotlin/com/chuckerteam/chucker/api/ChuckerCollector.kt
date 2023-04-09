package com.chuckerteam.chucker.api

import android.content.Context
import android.net.Uri
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.JsonConverter
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.support.TransactionDetailsHarSharable
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.writeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
) {
    private val retentionManager: RetentionManager = RetentionManager(context, retentionPeriod)
    private val notificationHelper: NotificationHelper = NotificationHelper(context)
    private val scope = MainScope()

    init {
        RepositoryProvider.initialize(context)
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    internal fun onRequestSent(transaction: HttpTransaction) {
        scope.launch {
            withContext(Dispatchers.IO) {
                RepositoryProvider.transaction().insertTransaction(transaction)
            }

            if (showNotification) {
                notificationHelper.show(transaction)
            }
            withContext(Dispatchers.IO) {
                retentionManager.doMaintenance()
            }
        }
    }

    /**
     * Call this method when you received the response of an HTTP request.
     * It must be called after [ChuckerCollector.onRequestSent].
     * @param transaction The sent HTTP transaction completed with the response
     */
    internal fun onResponseReceived(transaction: HttpTransaction) {
        scope.launch {
            val updated = withContext(Dispatchers.IO) {
                RepositoryProvider.transaction().updateTransaction(transaction)
            }
            if (showNotification && updated > 0) {
                notificationHelper.show(transaction)
            }
        }
    }

    /**
     * Export the Chucker transactions to a file. Please note that this function is blocking
     * and performs Disk I/O. Make sure you run it on a separate thread or coroutine.
     *
     * @param context Application context
     * @param startTimestamp The timestamp to read transactions from. Passing null means
     * transactions will not be limited by timestamp
     * @param exportFormat The export format: LOG or HAR
     * @return The content uri of a file with the transactions in or null if the export failed.
     */
    public fun writeTransactions(
        context: Context,
        startTimestamp: Long?,
        exportFormat: ExportFormat = ExportFormat.LOG
    ): Uri? {
        val transactions =
            RepositoryProvider.transaction().getTransactionsInTimeRange(startTimestamp)
        if (transactions.isEmpty()) {
            return null
        }

        val sharableTransactions = when (exportFormat) {
            ExportFormat.LOG -> {
                TransactionListDetailsSharable(transactions, encodeUrls = false)
            }
            ExportFormat.HAR -> {
                TransactionDetailsHarSharable(
                    JsonConverter.nonNullSerializerInstance
                        .toJson(
                            HarUtils.fromHttpTransactions(
                                transactions,
                                Creator(
                                    context.getString(R.string.chucker_name),
                                    context.getString(R.string.chucker_version)
                                )
                            )
                        )
                )
            }
        }
        return sharableTransactions.writeToFile(
            context = context,
            fileName = "api_transactions.${exportFormat.extension}"
        )
    }
}
