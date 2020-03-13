package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.support.distinctUntilChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class RecordedThrowableDatabaseRepository(
    private val database: ChuckerDatabase
) : RecordedThrowableRepository {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    override fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable> {
        return database.throwableDao().getById(id).distinctUntilChanged()
    }

    override fun deleteAllThrowables() {
        backgroundScope.launch { database.throwableDao().deleteAll() }
    }

    override fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>> {
        return database.throwableDao().getTuples()
    }

    override fun saveThrowable(throwable: RecordedThrowable) {
        backgroundScope.launch { database.throwableDao().insert(throwable) }
    }

    override fun deleteOldThrowables(threshold: Long) {
        backgroundScope.launch { database.throwableDao().deleteBefore(threshold) }
    }
}
