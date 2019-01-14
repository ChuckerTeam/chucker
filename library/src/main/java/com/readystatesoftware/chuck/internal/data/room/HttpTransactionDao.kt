package com.readystatesoftware.chuck.internal.data.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.HttpTransactionTuple

@Dao
internal interface HttpTransactionDao {

    @Query("SELECT id, requestDate, tookMs, protocol, method, host, path, scheme, responseCode, requestContentLength, responseContentLength, error FROM transactions ORDER BY requestDate DESC")
    fun getSortedTransactionsTuples(): LiveData<List<HttpTransactionTuple>>

    @Query("SELECT id, requestDate, tookMs, protocol, method, host, path, scheme, responseCode, requestContentLength, responseContentLength, error FROM transactions WHERE responseCode LIKE :codeQuery AND path LIKE :pathQuery ORDER BY requestDate DESC")
    fun getFilteredTransactionsTuples(codeQuery: String, pathQuery: String): LiveData<List<HttpTransactionTuple>>

    @Insert()
    fun insertTransaction(transaction: HttpTransaction): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTransaction(transaction: HttpTransaction): Int

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getRecordedTransaction(id: Long): LiveData<HttpTransaction>

    @Query("DELETE FROM transactions WHERE requestDate <= :threshold")
    fun deleteOldTransactions(threshold: Long)

}
