package com.rohitjakhar.composechucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.rohitjakhar.composechucker.internal.data.entity.HttpTransaction
import com.rohitjakhar.composechucker.internal.data.entity.HttpTransactionTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [HttpTransaction] and [HttpTransactionTuple]. Please use [HttpTransactionDatabaseRepository] that
 * uses Room and SqLite to run those operations.
 */
internal interface HttpTransactionRepository {

    suspend fun insertTransaction(transaction: HttpTransaction)

    suspend fun updateTransaction(transaction: HttpTransaction): Int

    suspend fun deleteOldTransactions(threshold: Long)

    suspend fun deleteAllTransactions()

    fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>>

    fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>>

    fun getTransaction(transactionId: Long): LiveData<HttpTransaction?>

    suspend fun getAllTransactions(): List<HttpTransaction>

    fun getTransactionsInTimeRange(minTimestamp: Long?): List<HttpTransaction>
}
