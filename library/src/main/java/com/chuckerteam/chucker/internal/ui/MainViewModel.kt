package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData<String>("")

    val transactions: LiveData<List<HttpTransactionTuple>> = Transformations.switchMap(currentFilter) { searchQuery ->
        with(RepositoryProvider.transaction()) {
            when {
                searchQuery.isNullOrBlank() -> {
                    getSortedTransactionTuples()
                }
                TextUtils.isDigitsOnly(searchQuery) -> {
                    getFilteredTransactionTuples(searchQuery, "")
                }
                else -> {
                    getFilteredTransactionTuples("", searchQuery)
                }
            }
        }
    }

    val errors: LiveData<List<RecordedThrowableTuple>> =
        Transformations.map(
            RepositoryProvider.throwable().getSortedThrowablesTuples()
        ) {
            it
        }

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun clearTransactions() {
        RepositoryProvider.transaction().deleteAllTransactions()
        NotificationHelper.clearBuffer()
    }

    fun clearErrors() {
        RepositoryProvider.throwable().deleteAllThrowables()
    }
}
