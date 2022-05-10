package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.*
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.combineLatest
import kotlinx.coroutines.flow.*

internal class TransactionViewModel(private val transactionId: Long) : ViewModel() {

    private val mutableEncodeUrl = MutableStateFlow(false)

    val encodeUrl: StateFlow<Boolean> = mutableEncodeUrl

    val transactionTitle: Flow<String> = RepositoryProvider.transaction()
        .getTransaction(transactionId,Transaction.Type.Http).map { it as HttpTransaction? }
        .combine(encodeUrl) { transaction, encodeUrl ->
            if (transaction != null) "${transaction.method} ${transaction.getFormattedPath(encode = encodeUrl)}" else ""
        }

    val doesUrlRequireEncoding: Flow<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId,Transaction.Type.Http).map { it as HttpTransaction? }
        .map { transaction ->
            if (transaction == null) {
                false
            } else {
                transaction.getFormattedPath(encode = true) != transaction.getFormattedPath(encode = false)
            }
        }

    val doesRequestBodyRequireEncoding: Flow<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId,Transaction.Type.Http).map { it as HttpTransaction? }
        .map { transaction ->
            transaction?.requestContentType?.contains("x-www-form-urlencoded", ignoreCase = true) ?: false
        }

    val transaction: StateFlow<HttpTransaction?>
        get() = RepositoryProvider.transaction()
            .getTransaction(transactionId, Transaction.Type.Http)
            .map { it as HttpTransaction? }.stateIn(viewModelScope, SharingStarted.Lazily,null)


    val formatRequestBody: Flow<Boolean> = doesRequestBodyRequireEncoding
        .combine(encodeUrl) { requiresEncoding, encodeUrl ->
            !(requiresEncoding && encodeUrl)
        }

    fun switchUrlEncoding() = encodeUrl(!mutableEncodeUrl.value)

    fun encodeUrl(encode: Boolean) {
        mutableEncodeUrl.value = encode
    }
}

internal class TransactionViewModelFactory(
    private val transactionId: Long = 0L
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return TransactionViewModel(transactionId) as T
    }
}
