package com.readystatesoftware.chuck.internal.data.repository

import android.arch.lifecycle.LiveData
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.HttpTransactionTuple
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple

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

    fun getRecordedThrowable(id: Long) : LiveData<RecordedThrowable>

}