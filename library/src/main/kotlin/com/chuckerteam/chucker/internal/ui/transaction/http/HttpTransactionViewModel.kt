package com.chuckerteam.chucker.internal.ui.transaction.http

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import kotlinx.coroutines.flow.*

internal class HttpTransactionViewModel(
    sharedViewModel: TransactionViewModel
) : ViewModel() {
    private val mutableEncodeUrl = MutableStateFlow(false)
    val encodeUrl: StateFlow<Boolean> = mutableEncodeUrl

    val transaction: StateFlow<HttpTransaction?> = sharedViewModel.transaction
        .map { it as HttpTransaction? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val transactionTitle: Flow<String> = sharedViewModel.transaction
        .map { it as HttpTransaction? }
        .combine(encodeUrl) { transaction, encodeUrl ->
            if (transaction != null) "${transaction.method} ${transaction.getFormattedPath(encode = encodeUrl)}" else ""
        }

    fun switchUrlEncoding() = encodeUrl(!mutableEncodeUrl.value)

    fun encodeUrl(encode: Boolean) {
        mutableEncodeUrl.value = encode
    }

    val doesRequestBodyRequireEncoding: Flow<Boolean> = sharedViewModel.transaction
        .map { transaction ->
            transaction as HttpTransaction?
            transaction?.requestContentType?.contains("x-www-form-urlencoded", ignoreCase = true)
                ?: false
        }

    val formatRequestBody: Flow<Boolean> = doesRequestBodyRequireEncoding
        .combine(encodeUrl) { requiresEncoding, encodeUrl ->
            !(requiresEncoding && encodeUrl)
        }

    val doesUrlRequireEncoding: Flow<Boolean> = sharedViewModel
        .transaction
        .map { transaction ->
            if (transaction == null) {
                false
            } else {
                transaction as HttpTransaction
                transaction.getFormattedPath(encode = true) != transaction.getFormattedPath(encode = false)
            }
        }
}


internal class HttpTransactionViewModelFactory(
    private val sharedViewModel: TransactionViewModel
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == HttpTransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return HttpTransactionViewModel(sharedViewModel) as T
    }
}
