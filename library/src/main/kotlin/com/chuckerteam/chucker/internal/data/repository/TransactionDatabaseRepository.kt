package com.chuckerteam.chucker.internal.data.repository

import android.text.TextUtils
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.lang.IllegalArgumentException

internal class TransactionDatabaseRepository(
    roomDatabase: ChuckerDatabase
) : TransactionRepository {
    private val requestsDao = roomDatabase.transactionDao()
    private val eventsDao = roomDatabase.eventTransactionDao()

    override suspend fun insertTransaction(transaction: Transaction) {
        when (transaction) {
            is HttpTransaction -> {
                val id = requestsDao.insert(transaction)
                transaction.id = id ?: 0
            }
            is EventTransaction -> {
                val id = eventsDao.insert(transaction)
                transaction.id = id ?: 0
            }
            is HttpTransactionTuple -> throw IllegalArgumentException("not supported")
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) : Int {
        return when(transaction) {
            is EventTransaction -> eventsDao.update(transaction)
            is HttpTransaction -> requestsDao.update(transaction)
            is HttpTransactionTuple -> throw IllegalArgumentException("not supported")
        }
    }

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

    override fun getTransaction(transactionId: Long, type: Transaction.Type): Flow<Transaction?> {
        return when(type) {
            Transaction.Type.Http -> requestsDao.getById(transactionId)
            Transaction.Type.Event -> eventsDao.getById(transactionId)
        }.distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }
}
