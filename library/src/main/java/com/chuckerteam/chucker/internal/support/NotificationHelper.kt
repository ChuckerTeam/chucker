package com.chuckerteam.chucker.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import java.util.HashSet

internal class NotificationHelper(val context: Context) {

    companion object {
        private const val TRANSACTIONS_CHANNEL_ID = "chucker_transactions"

        private const val TRANSACTION_NOTIFICATION_ID = 1138

        private const val BUFFER_SIZE = 10
        private const val INTENT_REQUEST_CODE = 11
        private val transactionBuffer = LongSparseArray<HttpTransaction>()
        private val transactionIdsSet = HashSet<Long>()

        fun clearBuffer() {
            synchronized(transactionBuffer) {
                transactionBuffer.clear()
                transactionIdsSet.clear()
            }
        }
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val transactionsScreenIntent by lazy {
        PendingIntent.getActivity(
            context,
            TRANSACTION_NOTIFICATION_ID,
            Chucker.getLaunchIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionsChannel = NotificationChannel(
                TRANSACTIONS_CHANNEL_ID,
                context.getString(R.string.chucker_network_notification_category),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(transactionsChannel))
        }
    }

    private fun addToBuffer(transaction: HttpTransaction) {
        if (transaction.id == 0L) {
            // Don't store Transactions with an invalid ID (0).
            // Transaction with an Invalid ID will be shown twice in the notification
            // with both the invalid and the valid ID and we want to avoid this.
            return
        }
        synchronized(transactionBuffer) {
            transactionIdsSet.add(transaction.id)
            transactionBuffer.put(transaction.id, transaction)
            if (transactionBuffer.size() > BUFFER_SIZE) {
                transactionBuffer.removeAt(0)
            }
        }
    }

    fun show(transaction: HttpTransaction) {
        addToBuffer(transaction)
        if (!BaseChuckerActivity.isInForeground) {
            val builder =
                NotificationCompat.Builder(context, TRANSACTIONS_CHANNEL_ID)
                    .setContentIntent(transactionsScreenIntent)
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chucker_ic_transaction_notification)
                    .setColor(ContextCompat.getColor(context, R.color.chucker_color_primary))
                    .setContentTitle(context.getString(R.string.chucker_http_notification_title))
                    .setAutoCancel(true)
                    .addAction(createClearAction())
            val inboxStyle = NotificationCompat.InboxStyle()
            synchronized(transactionBuffer) {
                var count = 0
                (transactionBuffer.size() - 1 downTo 0).forEach { i ->
                    val bufferedTransaction = transactionBuffer.valueAt(i)
                    if ((bufferedTransaction != null) && count < BUFFER_SIZE) {
                        if (count == 0) {
                            builder.setContentText(bufferedTransaction.notificationText)
                        }
                        inboxStyle.addLine(bufferedTransaction.notificationText)
                    }
                    count++
                }
                builder.setStyle(inboxStyle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setSubText(transactionIdsSet.size.toString())
                } else {
                    builder.setNumber(transactionIdsSet.size)
                }
            }
            notificationManager.notify(TRANSACTION_NOTIFICATION_ID, builder.build())
        }
    }

    private fun createClearAction():
        NotificationCompat.Action {
            val clearTitle = context.getString(R.string.chucker_clear)
            val deleteIntent = Intent(context, ClearDatabaseService::class.java)
            val intent = PendingIntent.getService(
                context,
                INTENT_REQUEST_CODE,
                deleteIntent,
                PendingIntent.FLAG_ONE_SHOT
            )
            return NotificationCompat.Action(R.drawable.chucker_ic_delete_white, clearTitle, intent)
        }

    fun dismissNotifications() {
        notificationManager.cancel(TRANSACTION_NOTIFICATION_ID)
    }
}
