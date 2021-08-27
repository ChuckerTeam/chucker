package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.*
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.combineLatest
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal class TransactionViewModel(transactionId: Long) : ViewModel() {

    private val mutableEncodeUrl = MutableLiveData<Boolean>(false)

    val encodeUrl: LiveData<Boolean> = mutableEncodeUrl

    val transactionTitle: LiveData<String> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .combineLatest(encodeUrl) { transaction, encodeUrl ->
            if (transaction != null) "${transaction.method} ${transaction.getFormattedPath(encode = encodeUrl)}" else ""
        }

    val doesUrlRequireEncoding: LiveData<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .map { transaction ->
            if (transaction == null) {
                false
            } else {
                transaction.getFormattedPath(encode = true) != transaction.getFormattedPath(encode = false)
            }
        }

    val doesRequestBodyRequireEncoding: LiveData<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .map { transaction ->
            transaction?.requestContentType?.contains("x-www-form-urlencoded", ignoreCase = true) ?: false
        }

    val transaction: LiveData<HttpTransaction?> = RepositoryProvider.transaction().getTransaction(transactionId)

    val formatRequestBody: LiveData<Boolean> = doesRequestBodyRequireEncoding
        .combineLatest(encodeUrl) { requiresEncoding, encodeUrl ->
            !(requiresEncoding && encodeUrl)
        }

    fun switchUrlEncoding() = encodeUrl(!encodeUrl.value!!)

    fun encodeUrl(encode: Boolean) {
        mutableEncodeUrl.value = encode
    }

    fun repeatRequest(okhttpClient: OkHttpClient, callback: Callback) {
        transaction.value?.let {
            try {
                val headers = it.getParsedRequestHeaders()
                val body = it.getFormattedRequestBody()

                val requestBuilder: Request.Builder = Request.Builder().url(it.url.toString())
                if (headers != null) {
                    for (header in headers) {
                        requestBuilder.addHeader(header.name, header.value)
                    }
                }
                if (it.method != null &&  it.requestContentType != null) {
                    requestBuilder.method(it.method!!, body.toRequestBody(it.requestContentType!!.toMediaType()))
                }
                okhttpClient.newCall(requestBuilder.build()).enqueue(callback)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}

internal class TransactionViewModelFactory(
    private val transactionId: Long = 0L
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == TransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return TransactionViewModel(transactionId) as T
    }
}
