package com.chuckerteam.chucker.api.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransactionTuple

@Dao
internal interface HttpTransactionDao {

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, " +
            "path, scheme, responseCode, requestContentLength, responseContentLength, error FROM " +
            "transactions ORDER BY requestDate DESC"
    )
    fun getSortedTuples(): LiveData<List<HttpTransactionTuple>>

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, " +
            "path, scheme, responseCode, requestContentLength, responseContentLength, error FROM " +
            "transactions WHERE responseCode LIKE :codeQuery AND path LIKE :pathQuery " +
            "ORDER BY requestDate DESC"
    )
    fun getFilteredTuples(codeQuery: String, pathQuery: String): LiveData<List<HttpTransactionTuple>>

    @Insert
    fun insert(transaction: HttpTransaction): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(transaction: HttpTransaction): Int

    @Query("DELETE FROM transactions")
    fun deleteAll()

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): LiveData<HttpTransaction>

    @Query("DELETE FROM transactions WHERE requestDate <= :threshold")
    fun deleteBefore(threshold: Long)
}
