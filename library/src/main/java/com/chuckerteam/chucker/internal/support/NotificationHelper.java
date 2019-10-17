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
package com.chuckerteam.chucker.internal.support;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.Chucker;
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction;
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable;
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity;

import java.util.HashSet;

public class NotificationHelper {

    private static final String CHANNEL_ID = "chucker";
    private static final int TRANSACTION_NOTIFICATION_ID = 1138;
    private static final int ERROR_NOTIFICATION_ID = 3546;
    private static final int BUFFER_SIZE = 10;

    private static final LongSparseArray<HttpTransaction> transactionBuffer = new LongSparseArray<>();
    // Set of all the seen transaction IDs. Used to update the notification count.
    private static final HashSet<Long> transactionIdsSet = new HashSet<>();

    private final Context context;
    private final NotificationManager notificationManager;

    public static void clearBuffer() {
        synchronized (transactionBuffer) {
            transactionBuffer.clear();
            transactionIdsSet.clear();
        }
    }

    private static void addToBuffer(HttpTransaction transaction) {
        if (transaction.getId() == 0) {
            // Don't store Transactions with an invalid ID (0).
            // Transaction with an Invalid ID will be shown twice in the notification
            // with both the invalid and the valid ID and we want to avoid this.
            return;
        }
        synchronized (transactionBuffer) {
            transactionIdsSet.add(transaction.getId());
            transactionBuffer.put(transaction.getId(), transaction);
            if (transactionBuffer.size() > BUFFER_SIZE) {
                transactionBuffer.removeAt(0);
            }
        }
    }

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID,
                            context.getString(R.string.chucker_notification_category),
                            NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    public void show(HttpTransaction transaction) {
        addToBuffer(transaction);
        if (!BaseChuckerActivity.isInForeground()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(PendingIntent.getActivity(context, TRANSACTION_NOTIFICATION_ID, Chucker.getLaunchIntent(context, Chucker.SCREEN_HTTP), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chucker_ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.chucker_primary_color))
                    .setContentTitle(context.getString(R.string.chucker_http_notification_title))
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Transaction.INSTANCE));
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            synchronized (transactionBuffer) {
                int count = 0;
                for (int i = transactionBuffer.size() - 1; i >= 0; i--) {
                    if (count < BUFFER_SIZE) {
                        if (count == 0) {
                            builder.setContentText(transactionBuffer.valueAt(i).getNotificationText());
                        }
                        inboxStyle.addLine(transactionBuffer.valueAt(i).getNotificationText());
                    }
                    count++;
                }
                builder.setAutoCancel(true);
                builder.setStyle(inboxStyle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setSubText(String.valueOf(transactionIdsSet.size()));
                } else {
                    builder.setNumber(transactionIdsSet.size());
                }
            }
            notificationManager.notify(TRANSACTION_NOTIFICATION_ID, builder.build());
        }
    }

    public void show(RecordedThrowable throwable) {
        if (!BaseChuckerActivity.isInForeground()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(PendingIntent.getActivity(context, ERROR_NOTIFICATION_ID, Chucker.getLaunchIntent(context, Chucker.SCREEN_ERROR), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chucker_ic_subject_white_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.chucker_status_error))
                    .setContentTitle(throwable.getClazz())
                    .setAutoCancel(true)
                    .setContentText(throwable.getMessage())
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Error.INSTANCE));
            notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build());
        }
    }

    @NonNull
    private NotificationCompat.Action createClearAction(ClearDatabaseService.ClearAction clearAction) {
        CharSequence clearTitle = context.getString(R.string.chucker_clear);
        Intent deleteIntent = new Intent(context, ClearDatabaseService.class);
        deleteIntent.putExtra(ClearDatabaseService.EXTRA_ITEM_TO_CLEAR, clearAction);
        PendingIntent intent = PendingIntent.getService(context, 11, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
        return new NotificationCompat.Action(R.drawable.chucker_ic_delete_white_24dp, clearTitle, intent);
    }

    public void dismissTransactionsNotification() {
        notificationManager.cancel(TRANSACTION_NOTIFICATION_ID);
    }

    public void dismissErrorsNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID);
    }
}
