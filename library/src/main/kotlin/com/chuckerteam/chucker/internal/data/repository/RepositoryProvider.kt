package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider.initialize
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

/**
 * A singleton to hold the [HttpTransactionRepository] instance.
 * Make sure you call [initialize] before accessing the stored instance.
 */
internal object RepositoryProvider {

    private var transactionRepository: HttpTransactionRepository? = null

    private var eventTransactionRepository: EventTransactionRepository? = null

    private var transactionRepo: TransactionRepository? = null

    fun transaction(): HttpTransactionRepository {
        return checkNotNull(transactionRepository) {
            "You can't access the transaction repository if you don't initialize it!"
        }
    }

    fun eventTransaction(): EventTransactionRepository {
        return checkNotNull(eventTransactionRepository) {
            "You can't access the event transaction repository if you don't initialize it!"
        }
    }

    fun transactionRepo(): TransactionRepository {
        return checkNotNull(transactionRepo) {
            "You can't access the transaction repository if you don't initialize it!"
        }
    }

    /**
     * Idempotent method. Must be called before accessing the repositories.
     */
    fun initialize(applicationContext: Context) {
        if (transactionRepository == null) {
            val db = ChuckerDatabase.create(applicationContext)
            transactionRepository = HttpTransactionDatabaseRepository(db)
            eventTransactionRepository = EventTransactionDatabaseRepository(db)
            transactionRepo = TransactionDatabaseRepository(db)
        }
    }

    /**
     * Cleanup stored singleton objects
     */
    @VisibleForTesting
    fun close() {
        transactionRepository = null
    }
}
