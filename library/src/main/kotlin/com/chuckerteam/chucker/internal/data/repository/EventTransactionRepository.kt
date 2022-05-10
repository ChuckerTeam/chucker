package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple

internal interface EventTransactionRepository {
    suspend fun insertTransaction(transaction: EventTransaction)
    suspend fun updateTransaction(transaction: EventTransaction): Int
    suspend fun deleteAllTransactions()
}
