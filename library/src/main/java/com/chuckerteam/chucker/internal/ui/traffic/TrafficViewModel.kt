package com.chuckerteam.chucker.internal.ui.traffic

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.ui.traffic.http.HttpTrafficRow
import com.chuckerteam.chucker.internal.ui.traffic.websocket.WebsocketLifecycleRow
import com.chuckerteam.chucker.internal.ui.traffic.websocket.WebsocketTrafficRow

class TrafficViewModel : ViewModel() {

    internal val networkTraffic: MediatorLiveData<List<TrafficRow>> = MediatorLiveData()
    private lateinit var httpData: LiveData<List<HttpTransactionTuple>>
    private lateinit var websocketData: LiveData<List<WebsocketTraffic>>
    private var currentFilter: String? = ""

    fun executeQuery(newText: String?): Boolean {
        networkTraffic.removeSource(httpData)
        networkTraffic.removeSource(websocketData)
        currentFilter = newText
        executeCurrentQuery()
        return true
    }

    fun executeCurrentQuery() {
        httpData = httpDataSource(currentFilter).also {
            networkTraffic.addSource(it) { combineTraffic() }
        }
        websocketData = websocketDataSource(currentFilter).also {
            networkTraffic.addSource(it) { combineTraffic() }
        }
    }

    private fun combineTraffic() {
        networkTraffic.value = mutableListOf<TrafficRow>().apply {
            addAll(httpData.value?.map { it.toTrafficRow() } ?: emptyList())
            addAll(websocketData.value?.map { it.toTrafficRow() } ?: emptyList())
            sortByDescending { it.timestamp }
        }
    }

    private fun httpDataSource(currentFilter: String?): LiveData<List<HttpTransactionTuple>> {
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

    private fun websocketDataSource(currentFilter: String?): LiveData<List<WebsocketTraffic>> {
        val repository = RepositoryProvider.websocket()
        return when {
            currentFilter.isNullOrEmpty() -> repository.getSortedTraffic()
            else -> repository.getFilteredTraffic(currentFilter)
        }
    }

    private fun String.isDigitsOnly(): Boolean = TextUtils.isDigitsOnly(this)

    private fun HttpTransactionTuple.toTrafficRow(): TrafficRow =
        HttpTrafficRow(this)

    private fun WebsocketTraffic.toTrafficRow(): TrafficRow = when {
        isData -> WebsocketTrafficRow(this)
        else -> WebsocketLifecycleRow(this)
    }
}