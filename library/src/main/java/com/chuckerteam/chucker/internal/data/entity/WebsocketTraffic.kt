package com.chuckerteam.chucker.internal.data.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.chuckerteam.chucker.R
import okhttp3.Request

@Entity(tableName = "websocket_traffic")
internal class WebsocketTraffic(
    @ColumnInfo(name = "operation") val operation: WebsocketOperation,
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
    val isData: Boolean
        get() = operation == WebsocketOperation.MESSAGE ||
                operation == WebsocketOperation.SEND
}

class WebsocketTrafficConverter {
    @TypeConverter
    fun operationFromString(db: String?): WebsocketOperation? =
        db?.let { WebsocketOperation.valueOf(it) }

    @TypeConverter
    fun stringFromOperation(operation: WebsocketOperation?): String? =
        operation?.toString()
}

enum class WebsocketOperation(val descriptionId: Int) {
    OPEN(R.string.chucker_ws_open),
    MESSAGE(R.string.chucker_ws_message),
    SEND(R.string.chucker_ws_send),
    FAILURE(R.string.chucker_ws_failure),
    CLOSING(R.string.chucker_ws_closing),
    CLOSED(R.string.chucker_ws_closed)
}

internal fun Request.asWebsocketTraffic(operation: WebsocketOperation) =
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
