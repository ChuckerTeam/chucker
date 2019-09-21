package com.chuckerteam.chucker.internal.data.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.Request

@Entity(tableName = "websocket_traffic")
internal class WebsocketTraffic(
    @ColumnInfo(name = "operation") val operation: String,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "timestamp") var timestamp: Long? = null,
    @ColumnInfo(name = "url") var url: String? = null,
    @ColumnInfo(name = "host") var host: String? = null,
    @ColumnInfo(name = "path") var path: String? = null,
    @ColumnInfo(name = "scheme") var scheme: String? = null,
    @ColumnInfo(name = "contentText") var contentText: String? = null,
    @ColumnInfo(name = "error") var error: String? = null,
    @ColumnInfo(name = "code") var code: Int? = null,
    @ColumnInfo(name = "reason") var reason: String? = null
) {
    override fun toString(): String {
        return "WebsocketTraffic(operation='$operation', id=$id, timestamp=$timestamp, " +
                "url=$url, host=$host, path=$path, scheme=$scheme, contentText=$contentText, " +
                "error=$error, code=$code, reason=$reason)"
    }
}

internal fun Request.asWebsocketTraffic(operation: String) =
    WebsocketTraffic(
        operation = operation,
        timestamp = System.currentTimeMillis()
    ).also { traffic ->
        val url = url().toString()
        val uri = Uri.parse(url)
        traffic.url = url
        traffic.host = uri.host
        traffic.path = "${uri.path}${uri.query?.let { "?$it" } ?: ""}"
        traffic.scheme = uri.scheme
    }
