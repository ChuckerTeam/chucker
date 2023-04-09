package com.chuckerteam.chucker.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple

@Dao
internal interface HttpTransactionDao {

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, path, scheme, responseCode, " +
            "requestPayloadSize, responsePayloadSize, error, graphQLDetected, graphQlOperationName FROM " +
            "transactions ORDER BY requestDate DESC"
    )
    fun getSortedTuples(): LiveData<List<HttpTransactionTuple>>

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, path, scheme, responseCode, " +
            "requestPayloadSize, responsePayloadSize, error, graphQLDetected, graphQlOperationName FROM " +
            "transactions WHERE responseCode LIKE :codeQuery AND (path LIKE :pathQuery OR " +
            "graphQlOperationName LIKE :graphQlQuery) ORDER BY requestDate DESC"
    )
    fun getFilteredTuples(
        codeQuery: String,
        pathQuery: String,
        graphQlQuery: String = ""
    ): LiveData<List<HttpTransactionTuple>>

    @Insert
    suspend fun insert(transaction: HttpTransaction): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: HttpTransaction): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): LiveData<HttpTransaction?>

    @Query("DELETE FROM transactions WHERE requestDate <= :threshold")
    suspend fun deleteBefore(threshold: Long): Int

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<HttpTransaction>

    @Query("SELECT * FROM transactions WHERE requestDate >= :timestamp")
    fun getTransactionsInTimeRange(timestamp: Long): List<HttpTransaction>
}
