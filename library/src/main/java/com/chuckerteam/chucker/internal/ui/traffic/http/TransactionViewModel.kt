package com.chuckerteam.chucker.internal.ui.traffic.http

import androidx.lifecycle.*
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

class TransactionViewModel(private val transactionId: Long) : ViewModel() {
    val transactionTitle: MutableLiveData<String> = MutableLiveData()
    internal var transaction: MutableLiveData<HttpTransaction> = MutableLiveData()

    fun loadTransaction() {
        RepositoryProvider.transaction().getTransaction(transactionId).observeOnce(Observer {
            transactionTitle.value = if (it != null) "${it.method} ${it.path}" else ""
            transaction.value = it
        })
    }

    private fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observeForever(object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}

class TransactionViewModelFactory(private val transactionId: Long = 0L) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Long::class.java).newInstance(transactionId)
    }
}