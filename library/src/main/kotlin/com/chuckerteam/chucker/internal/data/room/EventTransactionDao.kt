package com.chuckerteam.chucker.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple

@Dao
internal interface EventTransactionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: EventTransaction): Int

    @Query(
        "SELECT * FROM event_transactions ORDER BY receivedDate DESC"
    )
    suspend fun getAllSorted(): List<EventTransaction>

    @Insert
    suspend fun insert(transaction: EventTransaction): Long?

    @Query("DELETE FROM event_transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM event_transactions")
    suspend fun getAll(): List<EventTransaction>
}
