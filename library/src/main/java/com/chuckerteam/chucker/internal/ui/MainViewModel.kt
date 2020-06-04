package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.EXPORT_FILENAME
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData<String>("")

    val transactions: LiveData<List<HttpTransactionTuple>> = currentFilter.switchMap { searchQuery ->
        with(RepositoryProvider.transaction()) {
            when {
                searchQuery.isNullOrBlank() -> {
                    getSortedTransactionTuples()
                }
                TextUtils.isDigitsOnly(searchQuery) -> {
                    getFilteredTransactionTuples(searchQuery, "")
                }
                else -> {
                    getFilteredTransactionTuples("", searchQuery)
                }
            }
        }
    }

    val throwables: LiveData<List<RecordedThrowableTuple>> = RepositoryProvider.throwable()
        .getSortedThrowablesTuples()

    suspend fun getAllTransactions(): List<HttpTransaction>? = RepositoryProvider.transaction().getAllTransactions()

    suspend fun createExportFile(content: String, fileFactory: FileFactory): File = withContext(Dispatchers.IO) {
        val file = fileFactory.create(EXPORT_FILENAME)
        file.writeText(content)
        return@withContext file
    }

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun clearTransactions() {
        viewModelScope.launch {
            RepositoryProvider.transaction().deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
    }

    fun clearThrowables() {
        viewModelScope.launch {
            RepositoryProvider.throwable().deleteAllThrowables()
        }
    }
}
