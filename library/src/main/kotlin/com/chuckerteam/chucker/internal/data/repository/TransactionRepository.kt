package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.Transaction

internal interface TransactionRepository {
    suspend fun getSortedTransactionTuples(): List<Transaction>
    suspend fun getFilteredTransactionTuples(code: String, path: String): List<Transaction>
    suspend fun getAllTransactions() : List<Transaction>
    suspend fun deleteAllTransactions()
}
