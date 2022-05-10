package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

internal class EventTransactionDatabaseRepository(
    private val database: ChuckerDatabase
) : EventTransactionRepository {

    override suspend fun updateTransaction(transaction: EventTransaction): Int {
        return database.eventTransactionDao().update(transaction)
    }

}
