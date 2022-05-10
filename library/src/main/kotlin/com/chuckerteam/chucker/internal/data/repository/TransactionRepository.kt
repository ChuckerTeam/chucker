package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

internal interface TransactionRepository {
    fun getSortedTransactions(): Flow<List<Transaction>>
    fun getFilteredTransactions(query : String): Flow<List<Transaction>>
    suspend fun getAllTransactions() : List<Transaction>
    suspend fun deleteAllTransactions()
}
