package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.model.SearchQueryAndRequestTags
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData(SearchQueryAndRequestTags("", emptyList()))

    val transactions: LiveData<List<HttpTransactionTuple>> = currentFilter.switchMap { searchQueryAndRequestTags ->
        with(RepositoryProvider.transaction()) {
            when {
                searchQueryAndRequestTags.searchQuery.isBlank() -> {
                    getSortedTransactionTuples(searchQueryAndRequestTags.requestTags)
                }
                TextUtils.isDigitsOnly(searchQueryAndRequestTags.searchQuery) -> {
                    getFilteredTransactionTuples(
                        searchQueryAndRequestTags.searchQuery,
                        "",
                        searchQueryAndRequestTags.requestTags
                    )
                }
                else -> {
                    getFilteredTransactionTuples(
                        "",
                        searchQueryAndRequestTags.searchQuery,
                        searchQueryAndRequestTags.requestTags
                    )
                }
            }
        }
    }

    suspend fun getAllTransactions(): List<HttpTransaction> = RepositoryProvider.transaction().getAllTransactions()

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = currentFilter.value?.copy(searchQuery = searchQuery)
    }

    fun updateRequestTags(requestTags: List<String?>) {
        currentFilter.value = currentFilter.value?.copy(requestTags = requestTags)
    }

    fun clearTransactions() {
        viewModelScope.launch {
            RepositoryProvider.transaction().deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
    }
}
