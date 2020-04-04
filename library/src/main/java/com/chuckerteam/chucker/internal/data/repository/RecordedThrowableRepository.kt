package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [RecordedThrowable] and [RecordedThrowableTuple]. Please use [RecordedThrowableDatabaseRepository]
 * that uses Room and SqLite to run those operations.
 */
internal interface RecordedThrowableRepository {

    suspend fun saveThrowable(throwable: RecordedThrowable)

    suspend fun deleteOldThrowables(threshold: Long)

    suspend fun deleteAllThrowables()

    fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>>

    fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable>
}
