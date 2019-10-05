package com.chuckerteam.chucker.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic

@Dao
internal interface WebsocketTrafficDao {

    @Query(
        "SELECT id, operation, timestamp, url, ssl, host, path, scheme, contentText, " +
            "error, code, reason FROM websocket_traffic ORDER BY timestamp DESC"
    )
    fun getSortedTraffic(): LiveData<List<WebsocketTraffic>>

    @Query(
        "SELECT id, operation, timestamp, url, ssl, host, path, scheme, contentText, " +
            "error, code, reason FROM websocket_traffic WHERE path LIKE :pathQuery " +
            "ORDER BY timestamp DESC"
    )
    fun getFilteredTraffic(pathQuery: String): LiveData<List<WebsocketTraffic>>

    @Insert
    fun insert(traffic: WebsocketTraffic): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(traffic: WebsocketTraffic): Int

    @Query("DELETE FROM websocket_traffic")
    fun deleteAll()

    @Query(
        "SELECT id, operation, timestamp, url, ssl, host, path, scheme, contentText, " +
            "error, code, reason FROM websocket_traffic WHERE id = :id"
    )
    fun getById(id: Long): LiveData<WebsocketTraffic>

    @Query("DELETE FROM websocket_traffic WHERE timestamp <= :threshold")
    fun deleteBefore(threshold: Long)
}
