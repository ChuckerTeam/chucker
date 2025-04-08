package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [HttpTransaction] and [HttpTransactionTuple]. Please use [HttpTransactionDatabaseRepository] that
 * uses Room and SqLite to run those operations.
 */
@Suppress("TooManyFunctions")
internal interface HttpTransactionRepository {
    suspend fun insertTransaction(transaction: HttpTransaction)

    suspend fun updateTransaction(transaction: HttpTransaction): Int

    suspend fun deleteOldTransactions(threshold: Long)

    suspend fun deleteAllTransactions()

    fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>>

    fun getFilteredTransactionTuples(
        code: String,
        path: String,
    ): LiveData<List<HttpTransactionTuple>>

    fun getTransaction(transactionId: Long): LiveData<HttpTransaction?>

    suspend fun getAllTransactions(): List<HttpTransaction>

    fun getTransactionsInTimeRange(minTimestamp: Long?): List<HttpTransaction>

    /**
     * Deletes all transactions that match the given list of transaction IDs.
     *
     * @param selectedTransactions A list of transaction IDs to be deleted.
     */
    suspend fun deleteSelectedTransactions(selectedTransactions: List<Long>)

    /**
     * Retrieves a list of full [HttpTransaction] objects for the provided list of IDs.
     *
     * @param selectedTransactions A list of transaction IDs to fetch.
     * @return A list of [HttpTransaction] matching the given IDs.
     */
    suspend fun getSelectedTransactions(selectedTransactions: List<Long>): List<HttpTransaction>
}
