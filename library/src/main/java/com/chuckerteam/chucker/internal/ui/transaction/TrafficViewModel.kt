package com.chuckerteam.chucker.internal.ui.transaction

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

class TrafficViewModel : ViewModel() {

    internal val networkTraffic: MediatorLiveData<List<TrafficRow>> = MediatorLiveData()
    private lateinit var dataSource: LiveData<List<HttpTransactionTuple>>
    private var currentFilter: String? = ""

    fun executeCurrentQuery() {
        dataSource = getDataSource(currentFilter).also {
            networkTraffic.addSource(it) { data ->
                networkTraffic.value = data.map { t -> HttpTrafficRow(t) }
            }
        }
    }

    internal fun executeQuery(newText: String?): Boolean {
        currentFilter = newText
        networkTraffic.removeSource(dataSource)
        dataSource = getDataSource(currentFilter).also {
            networkTraffic.addSource(it) { data ->
                networkTraffic.value = data.map { t -> HttpTrafficRow(t) }
            }
        }
        return true
    }

    private fun getDataSource(currentFilter: String?): LiveData<List<HttpTransactionTuple>> {
        val repository = RepositoryProvider.transaction()
        return when {
            currentFilter.isNullOrEmpty() -> repository.getSortedTransactionTuples()
            currentFilter.isDigitsOnly() -> repository.getFilteredTransactionTuples(
                currentFilter,
                ""
            )
            else -> repository.getFilteredTransactionTuples("", currentFilter)
        }
    }

    private fun String.isDigitsOnly(): Boolean = TextUtils.isDigitsOnly(this)
}