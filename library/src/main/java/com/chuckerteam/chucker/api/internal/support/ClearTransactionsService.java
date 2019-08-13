package com.chuckerteam.chucker.api.internal.support;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider;

public class ClearTransactionsService extends IntentService {

    public static final int CLEAR_TRANSACTIONS = 0;
    public static final int CLEAR_ERRORS = 1;
    public static final String EXTRA_ITEM_TO_CLEAR = "EXTRA_ITEM_TO_CLEAR";

    public ClearTransactionsService() {
        super("Chucker-ClearTransactionsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int itemToClear = intent.getIntExtra(EXTRA_ITEM_TO_CLEAR, -1);
        switch (itemToClear) {
            case CLEAR_TRANSACTIONS: {
                RepositoryProvider.transaction().deleteAllTransactions();
                NotificationHelper.clearBuffer();
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.dismissTransactionsNotification();
                break;
            }
            case CLEAR_ERRORS: {
                RepositoryProvider.throwable().deleteAllThrowables();
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.dismissErrorsNotification();
                break;
            }
        }
    }

    @IntDef(value = {CLEAR_TRANSACTIONS, CLEAR_ERRORS})
    public @interface Clear {
    }
}