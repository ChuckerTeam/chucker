package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class ClearDatabaseService : JobIntentService() {
    private val scope = MainScope()

    override fun onHandleWork(intent: Intent) {
        RepositoryProvider.initialize(applicationContext)
        scope.launch {
            RepositoryProvider.transaction().deleteAllTransactions()
            NotificationHelper.clearBuffer()
            NotificationHelper(applicationContext).dismissNotifications()
        }
    }

    companion object {
        private const val CLEAN_DATABASE_JOB_ID = 123321

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ClearDatabaseService::class.java, CLEAN_DATABASE_JOB_ID, work)
        }
    }
}
