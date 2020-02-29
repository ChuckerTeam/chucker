package com.chuckerteam.chucker.internal.ui.error

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

internal class ErrorViewModel(
    throwableId: Long
) : ViewModel() {

    val throwable: LiveData<RecordedThrowable> = RepositoryProvider.throwable().getRecordedThrowable(throwableId)
}

internal class ErrorViewModelFactory(
    private val throwableId: Long = 0L
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == ErrorViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return ErrorViewModel(throwableId) as T
    }
}
