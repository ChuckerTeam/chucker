package com.chuckerteam.chucker.internal.data.room

import androidx.room.*
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EventTransactionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: EventTransaction): Int

    @Query(
        "SELECT * FROM event_transactions ORDER BY receivedDate DESC"
    )
    fun getAllSorted(): Flow<List<EventTransaction>>

    @Query(
        "SELECT * FROM " +
            "event_transactions WHERE title LIKE :query AND payload LIKE :query " +
            "ORDER BY receivedDate DESC"
    )
    fun getFiltered(query: String): Flow<List<EventTransaction>>

    @Insert
    suspend fun insert(transaction: EventTransaction): Long?

    @Query("DELETE FROM event_transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM event_transactions WHERE receivedDate <= :threshold")
    suspend fun deleteBefore(threshold: Long)

    @Query("SELECT * FROM event_transactions")
    suspend fun getAll(): List<EventTransaction>

    @Query("SELECT * FROM event_transactions WHERE id = :id")
    fun getById(id: Long): Flow<HttpTransaction?>
}
