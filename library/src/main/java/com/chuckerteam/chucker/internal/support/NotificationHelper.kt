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
package com.chuckerteam.chucker.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.data.entity.NotificationTextProducer
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

class NotificationHelper(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.chucker_notification_category),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    internal fun show(textProducer: NotificationTextProducer) {
        Log.d("##NOTE", "${textProducer.notificationId()} ${textProducer.notificationText(context)}")
        addToBuffer(textProducer)
        buildTrafficNotification()
    }

    internal fun show(throwable: RecordedThrowable) {
        if (!BaseChuckerActivity.isInForeground()) {
            val builder =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            ERROR_NOTIFICATION_ID,
                            Chucker.getLaunchIntent(context, Chucker.SCREEN_ERROR),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chucker_ic_subject_white_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.chucker_status_error))
                    .setContentTitle(throwable.clazz)
                    .setAutoCancel(true)
                    .setContentText(throwable.message)
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Error))
            notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
        }
    }

    private fun buildTrafficNotification() {
        if (!BaseChuckerActivity.isInForeground()) {
            val builder =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            TRANSACTION_NOTIFICATION_ID,
                            Chucker.getLaunchIntent(context, Chucker.SCREEN_HTTP),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chucker_ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.chucker_primary_color))
                    .setContentTitle(context.getString(R.string.chucker_http_notification_title))
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Transaction))
            val inboxStyle = NotificationCompat.InboxStyle()
            synchronized(transactionBuffer) {
                for ((count, i) in (transactionBuffer.size() - 1 downTo 0).withIndex()) {
                    if (count < BUFFER_SIZE) {
                        if (count == 0) {
                            builder.setContentText(transactionBuffer.valueAt(i).notificationText(context))
                        }
                        inboxStyle.addLine(transactionBuffer.valueAt(i).notificationText(context))
                    }
                }
                builder.setAutoCancel(true)
                    .setStyle(inboxStyle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setSubText(transactionIdsSet.size.toString())
                } else {
                    builder.setNumber(transactionIdsSet.size)
                }
            }
            notificationManager.notify(TRANSACTION_NOTIFICATION_ID, builder.build())
        }
    }

    private fun createClearAction(clearAction: ClearDatabaseService.ClearAction): NotificationCompat.Action {
        val clearTitle = context.getString(R.string.chucker_clear)
        val deleteIntent = Intent(context, ClearDatabaseService::class.java)
        deleteIntent.putExtra(ClearDatabaseService.EXTRA_ITEM_TO_CLEAR, clearAction)
        val intent =
            PendingIntent.getService(context, REQUEST_CODE, deleteIntent, PendingIntent.FLAG_ONE_SHOT)
        return NotificationCompat.Action(
            R.drawable.chucker_ic_delete_white_24dp,
            clearTitle,
            intent
        )
    }

    fun dismissTransactionsNotification() =
        notificationManager.cancel(TRANSACTION_NOTIFICATION_ID)

    fun dismissErrorsNotification() =
        notificationManager.cancel(ERROR_NOTIFICATION_ID)

    companion object {
        private const val REQUEST_CODE = 11
        private const val CHANNEL_ID = "chucker"
        private const val TRANSACTION_NOTIFICATION_ID = 1138
        private const val ERROR_NOTIFICATION_ID = 3546
        private const val BUFFER_SIZE = 10

        private val transactionBuffer = LongSparseArray<NotificationTextProducer>()
        // Set of all the seen transaction IDs. Used to update the notification count.
        private val transactionIdsSet = mutableSetOf<Long>()

        fun clearBuffer() {
            synchronized(transactionBuffer) {
                transactionBuffer.clear()
                transactionIdsSet.clear()
            }
        }

        private fun addToBuffer(textProducer: NotificationTextProducer) {
            if (textProducer.notificationId() == 0L) {
                // Don't store Transactions with an invalid ID (0).
                // Transaction with an Invalid ID will be shown twice in the notification
                // with both the invalid and the valid ID and we want to avoid this.
                return
            }
            synchronized(transactionBuffer) {
                transactionIdsSet.add(textProducer.notificationId())
                transactionBuffer.put(textProducer.notificationId(), textProducer)
                if (transactionBuffer.size() > BUFFER_SIZE) {
                    transactionBuffer.removeAt(0)
                }
            }
        }
    }
}
