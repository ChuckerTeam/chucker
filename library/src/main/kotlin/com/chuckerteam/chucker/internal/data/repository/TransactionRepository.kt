package com.chuckerteam.chucker.internal.data.repository

import com.chuckerteam.chucker.internal.data.entity.Transaction

internal interface TransactionRepository {
    suspend fun getAllTransactions() : List<Transaction>
}
