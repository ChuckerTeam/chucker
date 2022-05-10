package com.chuckerteam.chucker.internal.data.room

import androidx.room.*
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HttpTransactionDao {

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, " +
            "path, scheme, responseCode, requestPayloadSize, responsePayloadSize, error FROM " +
            "http_transactions ORDER BY requestDate DESC"
    )
    fun getSortedTuples(): Flow<List<HttpTransactionTuple>>

    @Query(
        "SELECT id, requestDate, tookMs, protocol, method, host, " +
            "path, scheme, responseCode, requestPayloadSize, responsePayloadSize, error FROM " +
            "http_transactions WHERE responseCode LIKE :codeQuery AND path LIKE :pathQuery " +
            "ORDER BY requestDate DESC"
    )
    fun getFilteredTuples(codeQuery: String, pathQuery: String): Flow<List<HttpTransactionTuple>>

    @Insert
    suspend fun insert(transaction: HttpTransaction): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: HttpTransaction): Int

    @Query("DELETE FROM http_transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM http_transactions WHERE id = :id")
    fun getById(id: Long): Flow<HttpTransaction?>

    @Query("DELETE FROM http_transactions WHERE requestDate <= :threshold")
    suspend fun deleteBefore(threshold: Long)

    @Query("SELECT * FROM http_transactions")
    suspend fun getAll(): List<HttpTransaction>
}
