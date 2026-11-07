package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider.initialize
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.data.room.MapRemoteRuleDatabase

/**
 * A singleton to hold the [HttpTransactionRepository] and [MapRemoteRuleRepository] instances.
 * Make sure you call [initialize] before accessing the stored instances.
 */
internal object RepositoryProvider {
    private var transactionRepository: HttpTransactionRepository? = null
    private var mapRemoteRuleRepository: MapRemoteRuleRepository? = null

    fun transaction(): HttpTransactionRepository =
        checkNotNull(transactionRepository) {
            "You can't access the transaction repository if you don't initialize it!"
        }

    fun mapRemoteRule(): MapRemoteRuleRepository =
        checkNotNull(mapRemoteRuleRepository) {
            "You can't access the Map Remote rule repository if you don't initialize it!"
        }

    /**
     * Idempotent method. Must be called before accessing the repositories.
     */
    fun initialize(applicationContext: Context) {
        if (transactionRepository == null) {
            val db = ChuckerDatabase.create(applicationContext)
            transactionRepository = HttpTransactionDatabaseRepository(db)
        }
        if (mapRemoteRuleRepository == null) {
            mapRemoteRuleRepository = MapRemoteRuleDatabaseRepository(MapRemoteRuleDatabase.create(applicationContext))
        }
    }

    /**
     * Cleanup stored singleton objects
     */
    @VisibleForTesting
    fun close() {
        transactionRepository = null
        mapRemoteRuleRepository = null
    }
}
