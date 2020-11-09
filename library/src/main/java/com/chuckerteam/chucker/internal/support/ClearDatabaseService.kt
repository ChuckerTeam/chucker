package com.chuckerteam.chucker.internal.support

import android.app.IntentService
import android.content.Intent
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class ClearDatabaseService : IntentService(CLEAN_DATABASE_SERVICE_NAME) {

    override fun onHandleIntent(intent: Intent?) {
        RepositoryProvider.initialize(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            RepositoryProvider.transaction().deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
        NotificationHelper(this).dismissNotifications()
    }

    companion object {
        const val CLEAN_DATABASE_SERVICE_NAME = "Chucker-ClearDatabaseService"
    }
}
