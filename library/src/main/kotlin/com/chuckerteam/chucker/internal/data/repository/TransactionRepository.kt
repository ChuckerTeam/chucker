package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

internal interface TransactionRepository {
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction): Int
    fun getSortedTransactions(): Flow<List<Transaction>>
    fun getFilteredTransactions(query: String): Flow<List<Transaction>>
    suspend fun getAllTransactions(): List<Transaction>
    suspend fun deleteAllTransactions()
    suspend fun deleteOldTransactions(threshold: Long)
    fun getTransaction(transactionId: Long, type: Transaction.Type): Flow<Transaction?>
}
