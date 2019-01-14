package com.readystatesoftware.chuck.internal.data.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple

@Dao
internal interface RecordedThrowableDao {

    @Query("SELECT id,tag,date,clazz,message FROM throwables ORDER BY date DESC")
    fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>>

    @Insert()
    fun insertRecordedThrowables(throwable: RecordedThrowable): Long?

    @Query("DELETE FROM throwables")
    fun deleteAllThrowables()

    @Query("SELECT * FROM throwables WHERE id = :id")
    fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable>

    @Query("DELETE FROM throwables WHERE date <= :threshold")
    fun deleteOldThrowables(threshold: Long)

}
