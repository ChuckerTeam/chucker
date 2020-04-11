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
import com.chuckerteam.chucker.internal.support.FileFactory
import com.chuckerteam.chucker.internal.support.NotificationHelper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData<String>("")
    private val exportFileName = "transactions.txt"

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

    fun getAllTransactions(resultHandler: (List<HttpTransaction>?) -> Unit) {
        viewModelScope.launch {
            val transactions = async { RepositoryProvider.transaction().getAllTransactions() }
            resultHandler(transactions.await())
        }
    }

    fun createExportFile(content: String, fileFactory: FileFactory, fileHandler: (File) -> Unit) {
        viewModelScope.launch {
            fileHandler(createFileForExport(content, fileFactory))
        }
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

    private suspend fun createFileForExport(content: String, cacheFileFactory: FileFactory): File {
        return withContext(Dispatchers.IO) {
            val file = cacheFileFactory.create(exportFileName)
            file.writeText(content)
            return@withContext file
        }
    }
}
