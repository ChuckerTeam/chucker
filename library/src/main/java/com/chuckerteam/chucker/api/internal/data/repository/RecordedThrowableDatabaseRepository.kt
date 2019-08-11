package com.chuckerteam.chucker.api.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowableTuple
import com.chuckerteam.chucker.api.internal.data.room.ChuckerDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class RecordedThrowableDatabaseRepository(
    private val database: ChuckerDatabase
) : RecordedThrowableRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    override fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable> {
        return database.throwableDao().getById(id)
    }

    override fun deleteAllThrowables() {
        executor.execute { database.throwableDao().deleteAll() }
    }

    override fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>> {
        return database.throwableDao().getTuples()
    }

    override fun saveThrowable(throwable: RecordedThrowable) {
        executor.execute { database.throwableDao().insert(throwable) }
    }

    override fun deleteOldThrowables(threshold: Long) {
        executor.execute { database.throwableDao().deleteBefore(threshold) }
    }
}
