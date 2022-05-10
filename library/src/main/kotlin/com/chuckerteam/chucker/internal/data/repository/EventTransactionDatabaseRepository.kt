package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

internal class EventTransactionDatabaseRepository(
    private val database: ChuckerDatabase
) : EventTransactionRepository {
    private val transactionDao get() = database.eventTransactionDao()

    override suspend fun insertTransaction(transaction: EventTransaction) {
        val id = transactionDao.insert(transaction)
        transaction.id = id ?: 0
    }

    override suspend fun updateTransaction(transaction: EventTransaction): Int {
        return database.eventTransactionDao().update(transaction)
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }

}
