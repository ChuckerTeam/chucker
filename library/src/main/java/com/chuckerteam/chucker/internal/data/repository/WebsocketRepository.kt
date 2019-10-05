package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic

internal interface WebsocketRepository {

    fun insertTraffic(traffic: WebsocketTraffic, after: () -> Unit)

    fun deleteOldTraffic(threshold: Long)

    fun deleteAllTraffic()

    fun getSortedTraffic(): LiveData<List<WebsocketTraffic>>

    fun getFilteredTraffic(path: String): LiveData<List<WebsocketTraffic>>

    fun getTraffic(id: Long): LiveData<WebsocketTraffic>
}
