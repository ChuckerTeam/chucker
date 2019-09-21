package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class WebsocketDatabaseRepository(private val database: ChuckerDatabase) : WebsocketRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    override fun getFilteredTraffic(path: String): LiveData<List<WebsocketTraffic>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return database.websocketTrafficDao().getFilteredTraffic(pathQuery)
    }

    override fun getTraffic(id: Long): LiveData<WebsocketTraffic> {
        return database.websocketTrafficDao().getById(id)
    }

    override fun getSortedTraffic(): LiveData<List<WebsocketTraffic>> {
        return database.websocketTrafficDao().getSortedTraffic()
    }

    override fun deleteAllTraffic() {
        executor.execute { database.websocketTrafficDao().deleteAll() }
    }

    override fun deleteOldTraffic(threshold: Long) {
        executor.execute { database.websocketTrafficDao().deleteBefore(threshold) }
    }

    override fun insertTraffic(traffic: WebsocketTraffic) {
        executor.execute {
            val id = database.websocketTrafficDao().insert(traffic)
            traffic.id = id ?: 0
        }
    }
}
