package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase

internal class TransactionDatabaseRepository(
    roomDatabase: ChuckerDatabase
) : TransactionRepository {
    private val requestsDao = roomDatabase.transactionDao()
    private val eventsDao = roomDatabase.eventTransactionDao()

    override suspend fun getSortedTransactionTuples(): List<Transaction> {
        val httpTransactions = requestsDao.getSortedTuples()
        val eventTransactions = eventsDao.getAllSorted()

        val sortedTransactions = mutableListOf<Transaction>()
        sortedTransactions.addAll(httpTransactions)
        sortedTransactions.addAll(eventTransactions)
        sortedTransactions.sortBy {
            return@sortBy it.time
        }

        return sortedTransactions
    }

    override suspend fun getFilteredTransactionTuples(
        code: String,
        path: String
    ): List<Transaction> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        val httpTransactions = requestsDao.getFilteredTuples("$code%", pathQuery)

        return httpTransactions
    }

    override suspend fun getAllTransactions(): List<Transaction> {
        val httpTransactions = requestsDao.getAll()
        val eventTransactions = eventsDao.getAll()

        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(httpTransactions)
        allTransactions.addAll(eventTransactions)
        allTransactions.sortBy { return@sortBy it.id }

        return allTransactions
    }

    override suspend fun deleteAllTransactions() {
        requestsDao.deleteAll()
        eventsDao.deleteAll()
    }
}
