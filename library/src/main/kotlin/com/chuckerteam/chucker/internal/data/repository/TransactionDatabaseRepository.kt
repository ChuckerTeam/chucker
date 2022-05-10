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
    private val httpDao = roomDatabase.transactionDao()
    private val eventsDao = roomDatabase.eventTransactionDao()

    override suspend fun insertTransaction(transaction: Transaction) {
        when (transaction) {
            is HttpTransaction -> {
                val id = httpDao.insert(transaction)
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
            is HttpTransaction -> httpDao.update(transaction)
            is HttpTransactionTuple -> throw IllegalArgumentException("not supported")
        }
    }

    override fun getSortedTransactions(): Flow<List<Transaction>> {
        return httpDao.getSortedTuples().combine(eventsDao.getAllSorted()) { a, b ->
            val combinedList = mutableListOf<Transaction>()
            combinedList.addAll(a)
            combinedList.addAll(b)
            combinedList.sortByDescending { it.time }
            return@combine combinedList
        }
    }

    override fun getFilteredTransactions(
        query : String
    ): Flow<List<Transaction>> {
        val httpFlow = if (TextUtils.isDigitsOnly(query)) {
            httpDao.getFilteredTuples("$query%", "%")
        } else {
            httpDao.getFilteredTuples("%", "%$query%")
        }

        return httpFlow.combine(eventsDao.getFiltered("%$query%")) { a,b ->
            val combinedList = mutableListOf<Transaction>()

            combinedList.addAll(a)
            combinedList.addAll(b)
            combinedList.sortByDescending { it.time }
            return@combine combinedList
        }
    }

    override suspend fun getAllTransactions(): List<Transaction> {
        val httpTransactions = httpDao.getAll()
        val eventTransactions = eventsDao.getAll()

        val allTransactions = mutableListOf<Transaction>()
        allTransactions.addAll(httpTransactions)
        allTransactions.addAll(eventTransactions)
        allTransactions.sortByDescending { it.time }

        return allTransactions
    }

    override suspend fun deleteAllTransactions() {
        httpDao.deleteAll()
        eventsDao.deleteAll()
    }

    override suspend fun deleteOldTransactions(threshold: Long) {
        httpDao.deleteBefore(threshold)
        eventsDao.deleteBefore(threshold)
    }

    override fun getTransaction(transactionId: Long, type: Transaction.Type): Flow<Transaction?> {
        return when(type) {
            Transaction.Type.Http -> httpDao.getById(transactionId)
            Transaction.Type.Event -> eventsDao.getById(transactionId)
        }.distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }
}
