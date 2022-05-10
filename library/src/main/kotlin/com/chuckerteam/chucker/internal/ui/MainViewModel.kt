package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.*
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {

    val transactions = MutableSharedFlow<List<Transaction>>()

    suspend fun getAllTransactions(): List<Transaction> =
        RepositoryProvider.transactionRepo().getAllTransactions()

    fun updateItemsFilter(searchQuery: String) {
        when {
            searchQuery.isBlank() -> {
                viewModelScope.launch {
                    transactions.emit(
                        RepositoryProvider.transactionRepo().getSortedTransactionTuples()
                    )
                }
            }
            TextUtils.isDigitsOnly(searchQuery) -> {
                viewModelScope.launch {
                    transactions.emit(
                        RepositoryProvider.transactionRepo()
                            .getFilteredTransactionTuples(searchQuery, "")
                    )
                }
            }
            else -> {
                viewModelScope.launch {
                    transactions.emit(
                        RepositoryProvider.transactionRepo()
                            .getFilteredTransactionTuples("", searchQuery)
                    )
                }
            }
        }
    }

    fun clearTransactions() {
        viewModelScope.launch {
            RepositoryProvider.transactionRepo().deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
    }
}
