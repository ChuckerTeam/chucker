package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.observeOnce

class TransactionViewModel(private val transactionId: Long) : ViewModel() {
    val transactionTitle: MutableLiveData<String> = MutableLiveData()
    internal var transaction: MutableLiveData<HttpTransaction> = MutableLiveData()

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

class TransactionViewModelFactory(private val transactionId: Long = 0L) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Long::class.java).newInstance(transactionId)
    }
}
