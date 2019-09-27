package com.chuckerteam.chucker.internal.ui.traffic.websocket

import android.view.View
import android.widget.TextView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.entity.TrafficType.WEBSOCKET_LIFECYCLE
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.ui.traffic.TrafficRow
import com.chuckerteam.chucker.internal.ui.traffic.TrafficViewHolder
import java.text.DateFormat

class WebsocketLifecycleViewHolder(view: View, listener: (Long, Int, TrafficType) -> Unit) :
    TrafficViewHolder(view, listener) {
    private val operation = view.findViewById<TextView>(R.id.chuckerWebsocketOperation)
    private val path = view.findViewById<TextView>(R.id.chuckerPath)
    private val host = view.findViewById<TextView>(R.id.chuckerHost)
    private val timestamp = view.findViewById<TextView>(R.id.chuckerTimestamp)

    override fun bind(trafficRow: TrafficRow) {
        val lifecycle = (trafficRow as WebsocketLifecycleRow).traffic
        timestamp.text = DateFormat.getTimeInstance().format(lifecycle.timestamp)
        operation.text = lifecycle.operation
        path.text = lifecycle.path
        host.text = lifecycle.host
    }
}

@Suppress("EqualsOrHashCode")
internal class WebsocketLifecycleRow(val traffic: WebsocketTraffic) :
    TrafficRow {
    override val id: Long = traffic.id
    override val timestamp: Long = traffic.timestamp ?: 0L
    override val type = WEBSOCKET_LIFECYCLE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebsocketTrafficRow

        if (traffic != other.traffic) return false

        return true
    }
}