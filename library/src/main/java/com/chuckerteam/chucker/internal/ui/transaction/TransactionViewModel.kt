package com.chuckerteam.chucker.internal.ui.transaction

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.combineLatest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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

    fun repeatRequest(context: AppCompatActivity) {
        transaction.value?.let {
            try {
                val redactHeaders: Set<String> = HashSet()
                val chuckerCollector = ChuckerCollector(context, true, RetentionManager.Period.ONE_HOUR)
                val chuckerInterceptor = ChuckerInterceptor.Builder(context).redactHeaders(redactHeaders)
                    .maxContentLength(25000000L).collector(chuckerCollector).build()
                val okhttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(chuckerInterceptor).build()

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
                okhttpClient.newCall(requestBuilder.build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        context.runOnUiThread {
                            e.message?.let { it1 -> Log.d(it.url, it1) }
                            Toast.makeText(context, context.getString(R.string.chucker_request_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onResponse(call: Call, response: Response) {
                        context.runOnUiThread {
                            response.body?.string()?.let { it1 -> Log.d(it.url, it1) }
                            Toast.makeText(context, context.getString(R.string.chucker_request_complete), Toast.LENGTH_LONG).show()
                        }
                    }
                })
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
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
