package com.chuckerteam.chucker.internal.data.room

import androidx.room.*
import com.chuckerteam.chucker.internal.data.entity.EventTransaction

@Dao
internal interface EventTransactionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: EventTransaction): Int

    @Insert
    suspend fun insert(transaction: EventTransaction): Long?

    @Query("DELETE FROM event_transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM event_transactions")
    suspend fun getAll(): List<EventTransaction>
}
