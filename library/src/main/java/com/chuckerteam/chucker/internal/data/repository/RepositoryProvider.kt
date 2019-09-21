package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider.initialize
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

/**
 * A singleton to hold the [ChuckerRepository] instance. Make sure you call [initialize] before
 * accessing the stored instance.
 */
internal object RepositoryProvider {

    private var transactionRepository: HttpTransactionRepository? = null
    private var websocketRepository: WebsocketRepository? = null
    private var throwableRepository: RecordedThrowableRepository? = null

    @JvmStatic fun transaction(): HttpTransactionRepository {
        return checkNotNull(transactionRepository) {
            "You can't access the transaction repository if you don't initialize it!"
        }
    }

    @JvmStatic fun websocket(): WebsocketRepository {
        return checkNotNull(websocketRepository) {
            "You can't access the websocket repository if you don't initialize it!"
        }
    }

    @JvmStatic fun throwable(): RecordedThrowableRepository {
        return checkNotNull(throwableRepository) {
            "You can't access the throwable repository if you don't initialize it!"
        }
    }

    /**
     * Idempotent method. Must be called before accessing the repositories.
     */
    @JvmStatic
    fun initialize(context: Context) {
        if (transactionRepository == null || throwableRepository == null || websocketRepository == null) {
            val db = ChuckerDatabase.create(context)
            websocketRepository = WebsocketDatabaseRepository(db)
            transactionRepository = HttpTransactionDatabaseRepository(db)
            throwableRepository = RecordedThrowableDatabaseRepository(db)
        }
    }
}
