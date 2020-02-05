package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

internal class TransactionViewModel(transactionId: Long) : ViewModel() {

    val transactionTitle: LiveData<String> =
        Transformations.map(RepositoryProvider.transaction().getTransaction(transactionId)) {
            if (it != null) "${it.method} ${it.path}" else ""
        }
    val transaction: LiveData<HttpTransaction> =
        Transformations.map(RepositoryProvider.transaction().getTransaction(transactionId)) {
            it
        }
}

internal class TransactionViewModelFactory(private val transactionId: Long = 0L) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == TransactionViewModel::class.java) { "Cannot create $modelClass" }
        return TransactionViewModel(transactionId) as T
    }
}
