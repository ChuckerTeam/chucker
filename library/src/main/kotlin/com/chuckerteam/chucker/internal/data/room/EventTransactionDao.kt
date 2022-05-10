package com.chuckerteam.chucker.internal.data.room

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.chuckerteam.chucker.internal.data.entity.EventTransaction

@Dao
internal interface EventTransactionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(transaction: EventTransaction): Int
}
