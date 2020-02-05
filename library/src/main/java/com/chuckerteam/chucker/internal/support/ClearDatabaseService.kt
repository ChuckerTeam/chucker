package com.chuckerteam.chucker.internal.support

import android.app.IntentService
import android.content.Intent
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import java.io.Serializable

internal class ClearDatabaseService : IntentService(CLEAN_DATABASE_SERVICE_NAME) {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.getSerializableExtra(EXTRA_ITEM_TO_CLEAR)) {
            is ClearAction.Transaction -> {
                RepositoryProvider.initialize(applicationContext)
                RepositoryProvider.transaction().deleteAllTransactions()
                NotificationHelper.clearBuffer()
                NotificationHelper(this).dismissTransactionsNotification()
            }
            is ClearAction.Error -> {
                RepositoryProvider.initialize(applicationContext)
                RepositoryProvider.throwable().deleteAllThrowables()
                NotificationHelper(this).dismissErrorsNotification()
            }
        }
    }

    sealed class ClearAction : Serializable {
        object Transaction : ClearAction()
        object Error : ClearAction()
    }

    companion object {
        const val CLEAN_DATABASE_SERVICE_NAME = "Chucker-ClearDatabaseService"
        const val EXTRA_ITEM_TO_CLEAR = "EXTRA_ITEM_TO_CLEAR"
    }
}
