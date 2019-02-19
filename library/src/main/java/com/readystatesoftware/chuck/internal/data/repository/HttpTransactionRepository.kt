package com.readystatesoftware.chuck.internal.data.repository

import android.arch.lifecycle.LiveData
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.HttpTransactionTuple
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [HttpTransaction] and [HttpTransactionTuple]. Please use [ChuckerDatabaseRepository] that
 * uses Room and SqLite to run those operations.
 */
internal interface HttpTransactionRepository {

    fun insertTransaction(transaction: HttpTransaction)

    fun updateTransaction(transaction: HttpTransaction) : Int


    fun deleteOldTransactions(threshold: Long)

    fun deleteAllTransactions()


    fun getSortedTransactionTuples() : LiveData<List<HttpTransactionTuple>>

    fun getFilteredTransactionTuples(code : String, path : String) : LiveData<List<HttpTransactionTuple>>

    fun getTransaction(transactionId: Long) : LiveData<HttpTransaction>

}