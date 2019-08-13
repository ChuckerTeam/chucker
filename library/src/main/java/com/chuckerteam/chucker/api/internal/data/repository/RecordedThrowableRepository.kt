package com.chuckerteam.chucker.api.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowableTuple

/**
 * Repository Interface representing all the operations that are needed to let Chucker work
 * with [RecordedThrowable] and [RecordedThrowableTuple]. Please use [ChuckerDatabaseRepository]
 * that uses Room and SqLite to run those operations.
 */
internal interface RecordedThrowableRepository {

    fun saveThrowable(throwable: RecordedThrowable)

    fun deleteOldThrowables(threshold: Long)

    fun deleteAllThrowables()

    fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>>

    fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable>
}
