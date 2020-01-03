package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.observeOnce

internal class TransactionViewModel(private val transactionId: Long) : ViewModel() {
    internal val transactionTitle: MutableLiveData<String> = MutableLiveData()
    internal val transaction: MutableLiveData<HttpTransaction> = MutableLiveData()

    fun loadTransaction() {
        RepositoryProvider.transaction()
            .getTransaction(transactionId)
            .observeOnce(
                Observer {
                    transactionTitle.value = if (it != null) "${it.method} ${it.path}" else ""
                    transaction.value = it
                }
            )
    }
}

internal class TransactionViewModelFactory(private val transactionId: Long = 0L) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Long::class.java).newInstance(transactionId)
    }
}
