package com.chuckerteam.chucker.internal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.api.Group
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.launch
import java.util.UUID

internal class MainViewModel(private val transaction: HttpTransactionRepository) : ViewModel() {

    private val currentFilter = MutableLiveData("")
    private val groups = MutableLiveData<MutableList<Group>>()

    private val liveDataMerger = MediatorLiveData<String>()

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

    // Maybe there's a better way to do this that I'm not seeing at the moment (I'm very sleepy).
    // Basically I need the mediatorLiveData to change its value when the group or filter livedata
    // changes their values, to fetch the DB with the switchMap. The best I could think for now is
    // pass a MutableLiveData via param to getSortedTransactionTuples and
    // getFilteredTransactionTuples and change the value from there, but IDK if it's the best
    // option. If you could suggest anything (or think that pass a mutableLiveData via param is
    // the best option), please add your suggestion =].
    private fun fetchTransactions() {
        liveDataMerger.value = UUID.randomUUID().toString()
    }

    val transactions: LiveData<List<HttpTransactionTuple>> = liveDataMerger.switchMap {
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
