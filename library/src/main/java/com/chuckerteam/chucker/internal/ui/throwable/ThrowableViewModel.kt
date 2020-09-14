package com.chuckerteam.chucker.internal.ui.throwable

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

internal class ThrowableViewModel(
    throwableId: Long
) : ViewModel() {

    val throwable: LiveData<RecordedThrowable> = RepositoryProvider.throwable().getRecordedThrowable(throwableId)
}

internal class ThrowableViewModelFactory(
    private val throwableId: Long = 0L
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == ThrowableViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return ThrowableViewModel(throwableId) as T
    }
}
