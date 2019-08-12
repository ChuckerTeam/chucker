package com.chuckerteam.chucker.api.internal.data.repository

import android.content.Context
import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider.initialize
import com.chuckerteam.chucker.api.internal.data.room.ChuckerDatabase

/**
 * A singleton to hold the [ChuckerRepository] instance. Make sure you call [initialize] before
 * accessing the stored instance.
 */
internal object RepositoryProvider {

    private var transactionRepository: HttpTransactionRepository? = null
    private var throwableRepository: RecordedThrowableRepository? = null

    @JvmStatic fun transaction(): HttpTransactionRepository {
        return checkNotNull(transactionRepository) {
            "You can't access the transaction repository if you don't initialize it!"
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
        if (transactionRepository == null || throwableRepository == null) {
            val db = ChuckerDatabase.create(context)
            transactionRepository = HttpTransactionDatabaseRepository(db)
            throwableRepository = RecordedThrowableDatabaseRepository(db)
        }
    }
}
