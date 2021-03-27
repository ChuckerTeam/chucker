package com.chuckerteam.chucker.internal.support

import android.app.IntentService
import android.content.Intent
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class ClearDatabaseService : IntentService(CLEAN_DATABASE_SERVICE_NAME) {
    private val scope = MainScope()

    override fun onHandleIntent(intent: Intent?) {
        RepositoryProvider.initialize(applicationContext)
        scope.launch {
            RepositoryProvider.transaction().deleteAllTransactions()
            NotificationHelper.clearBuffer()
            NotificationHelper(applicationContext).dismissNotifications()
        }
    }

    companion object {
        const val CLEAN_DATABASE_SERVICE_NAME = "Chucker-ClearDatabaseService"
    }
}
