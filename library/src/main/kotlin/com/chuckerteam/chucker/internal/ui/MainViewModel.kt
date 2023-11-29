package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.support.distinctUntilChanged
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData("")
    private val selectedItemId:  MutableLiveData<MutableList<Long>> = MutableLiveData<MutableList<Long>>(mutableListOf())

    private var _isItemSelected = MutableLiveData<Boolean>(false)
    val isItemSelected = _isItemSelected.distinctUntilChanged()

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
                _isItemSelected.value = selectedItemId.value.isNullOrEmpty().not() == true
            } else {
                selectedItemId.value?.add(itemId)
                _isItemSelected.value = true
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
                _isItemSelected.value = false
                RepositoryProvider.transaction().deleteSelectedTransactions(selectedItemId.value!!)
            } else {
                RepositoryProvider.transaction().deleteAllTransactions()
            }
        }
        NotificationHelper.clearBuffer()
    }

}
