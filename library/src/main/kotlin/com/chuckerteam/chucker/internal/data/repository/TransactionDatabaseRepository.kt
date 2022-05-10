package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

internal class TransactionDatabaseRepository(
    roomDatabase: ChuckerDatabase
) : TransactionRepository {
    private val requestsDao = roomDatabase.transactionDao()
    private val eventsDao = roomDatabase.eventTransactionDao()

    override suspend fun getAllTransactions(): List<Transaction> {
        val httpTransactions = requestsDao.getAll()
        val eventTransactions = eventsDao.getAll()

        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(httpTransactions)
        allTransactions.addAll(eventTransactions)
        allTransactions.sortBy { return@sortBy it.id }

        return allTransactions
    }
}
