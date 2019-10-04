package com.chuckerteam.chucker.internal.ui.traffic.websocket

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.entity.WebsocketOperation.MESSAGE
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.observeOnce

class WebsocketTrafficViewModel(
    private val transactionId: Long,
    private val send: String,
    private val message: String
) : ViewModel() {
    val trafficTitle: MutableLiveData<String> = MutableLiveData()
    internal var traffic: MutableLiveData<WebsocketTraffic> = MutableLiveData()

    fun loadWebsocketTraffic() {
        RepositoryProvider.websocket().getTraffic(transactionId).observeOnce(Observer {
            trafficTitle.value =
                if (it != null) {
                    val operationName = if (it.operation == MESSAGE) message else send
                    "$operationName ${it.path}"
                } else ""
            traffic.value = it
        })
    }
}

class WebsocketTrafficViewModelFactory(
    private val transactionId: Long = 0L,
    private val send: String,
    private val message: String
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Long::class.java, String::class.java, String::class.java)
            .newInstance(transactionId, send, message)
    }
}