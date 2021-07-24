package com.chuckerteam.chucker.internal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.api.Group
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.launch

internal class MainViewModel(private val transaction: HttpTransactionRepository) : ViewModel() {

    private val currentFilter = MutableLiveData("")
    private val groups = MutableLiveData<MutableList<Group>>()

    private val liveDataMerger = MediatorLiveData<List<HttpTransactionTuple>>()

    init {
        liveDataMerger.addSource(
            currentFilter
        ) {
            fetchTransactions()
        }
        liveDataMerger.addSource(
            groups
        ) {
            fetchTransactions()
        }
    }

    private fun fetchTransactions() {
        val searchQuery = currentFilter.value
        val groups = groups.value ?: listOf()
        with(transaction) {
            when {
                searchQuery.isNullOrBlank() && groups.isNullOrEmpty() -> {
                    getSortedTransactionTuples()
                }
                searchQuery!!.isDigitsOnly() -> {
                    getFilteredTransactionTuples("", searchQuery, groups.map { it.urls }.flatten())
                }
                else -> {
                    getFilteredTransactionTuples(searchQuery, "", groups.map { it.urls }.flatten())
                }
            }
        }
    }

    val transactions: LiveData<List<HttpTransactionTuple>> = liveDataMerger

    suspend fun getAllTransactions(): List<HttpTransaction> = transaction.getAllTransactions()

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun clearTransactions() {
        viewModelScope.launch {
            transaction.deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
    }

    @Suppress("SpreadOperator")
    fun addGroup(group: Group) {
        val groupValue = groups.value ?: mutableListOf()
        groups.value = mutableListOf(*groupValue.toTypedArray(), group)
    }

    private fun String.isDigitsOnly(): Boolean {
        return this.all { it in '0'..'9' }
    }
}
