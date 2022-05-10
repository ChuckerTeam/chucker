package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.*
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.combineLatest
import kotlinx.coroutines.flow.*

internal class TransactionViewModel(
    val transactionId: Long,
    val transactionType: Transaction.Type
) : ViewModel() {
    val transaction: Flow<Transaction?>
        get() = RepositoryProvider.transaction()
            .getTransaction(transactionId, transactionType)
}

internal class TransactionViewModelFactory(
    private val transactionId: Long = 0L,
    private val transactionType: Transaction.Type = Transaction.Type.Http
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return TransactionViewModel(transactionId, transactionType) as T
    }
}
