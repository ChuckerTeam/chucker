package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.EventTransaction

internal interface EventTransactionRepository {
    suspend fun insertTransaction(transaction: EventTransaction)
    suspend fun updateTransaction(transaction: EventTransaction): Int
}
