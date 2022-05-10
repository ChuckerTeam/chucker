package com.chuckerteam.chucker.internal.data.repository

import android.text.TextUtils
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

internal class TransactionDatabaseRepository(
    roomDatabase: ChuckerDatabase
) : TransactionRepository {
    private val requestsDao = roomDatabase.transactionDao()
    private val eventsDao = roomDatabase.eventTransactionDao()

    override fun getSortedTransactions(): Flow<List<Transaction>> {
        return requestsDao.getSortedTuples().combine(eventsDao.getAllSorted()) { a,b ->
            val combinedList = mutableListOf<Transaction>()
            combinedList.addAll(a)
            combinedList.addAll(b)
            combinedList.sortBy { it.time }
            return@combine combinedList
        }
    }

    override fun getFilteredTransactions(
        query : String
    ): Flow<List<Transaction>> {
        val httpFlow = if (TextUtils.isDigitsOnly(query)) {
            requestsDao.getFilteredTuples("$query%", "%")
        } else {
            requestsDao.getFilteredTuples("%", "%$query%")
        }

        return httpFlow.combine(eventsDao.getFiltered("%$query%")) { a,b ->
            val combinedList = mutableListOf<Transaction>()

            combinedList.addAll(a)
            combinedList.addAll(b)
            combinedList.sortBy { it.time }
            return@combine combinedList
        }
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

    override suspend fun deleteOldTransactions(threshold: Long) {
        requestsDao.deleteBefore(threshold)
        eventsDao.deleteBefore(threshold)
    }
}
