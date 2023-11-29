package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData("")
    private val selectedItemId:  MutableLiveData<MutableList<Long>> = MutableLiveData<MutableList<Long>>(mutableListOf())

    val isItemSelected = selectedItemId.switchMap {
        liveData<Boolean> {
            emit(selectedItemId.value.isNullOrEmpty().not())
        }
    }

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

    fun selectItem(itemId: Long) {
        viewModelScope.launch {
            if (selectedItemId.value?.contains(itemId) == true) {
                selectedItemId.value?.remove(itemId)
            } else {
                selectedItemId.value?.add(itemId)
            }
        }
    }

    suspend fun getAllTransactions(): List<HttpTransaction> {
        return if (isItemSelected.value == true) {
            RepositoryProvider.transaction().getSelectedTransactions(selectedItemId.value!!)
        } else {
            RepositoryProvider.transaction().getAllTransactions()
        }
    }

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun clearTransactions() {
        viewModelScope.launch {
            if (isItemSelected.value == true) {
                RepositoryProvider.transaction().deleteSelectedTransactions(selectedItemId.value!!)
            } else {
                RepositoryProvider.transaction().deleteAllTransactions()
            }
        }
        NotificationHelper.clearBuffer()
    }

}
