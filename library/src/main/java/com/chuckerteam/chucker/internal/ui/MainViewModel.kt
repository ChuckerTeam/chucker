package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData<String>("")

    val transactions: LiveData<List<HttpTransactionTuple>> = currentFilter.switchMap { searchQuery ->
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

    val errors: LiveData<List<RecordedThrowableTuple>> = RepositoryProvider.throwable()
        .getSortedThrowablesTuples()

    private val mutableEncodeUrls = MutableLiveData<Boolean>(false)

    val encodeUrls: LiveData<Boolean> = mutableEncodeUrls

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun encodeUrls(encode: Boolean) {
        mutableEncodeUrls.value = encode
    }

    fun clearTransactions() {
        RepositoryProvider.transaction().deleteAllTransactions()
        NotificationHelper.clearBuffer()
    }

    fun clearErrors() {
        RepositoryProvider.throwable().deleteAllThrowables()
    }
}
