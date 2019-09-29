package com.chuckerteam.chucker.internal.ui.traffic.websocket

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.entity.TrafficType.WEBSOCKET_TRAFFIC
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.support.formatBytes
import com.chuckerteam.chucker.internal.ui.traffic.TrafficRow
import com.chuckerteam.chucker.internal.ui.traffic.TrafficViewHolder
import java.text.DateFormat

class WebsocketTrafficViewHolder(view: View, listener: (Long, Int, TrafficType) -> Unit) :
    TrafficViewHolder(view, listener) {
    private val operation = view.findViewById<TextView>(R.id.chuckerWebsocketOperation)
    private val path = view.findViewById<TextView>(R.id.chuckerPath)
    private val host = view.findViewById<TextView>(R.id.chuckerHost)
    private val timestamp = view.findViewById<TextView>(R.id.chuckerTimestamp)
    private val size = view.findViewById<TextView>(R.id.chuckerSize)
    private val ssl = view.findViewById<ImageView>(R.id.chuckerSsl)

    override fun bind(trafficRow: TrafficRow) {
        val websocketTraffic = (trafficRow as WebsocketTrafficRow).traffic
        timestamp.text = DateFormat.getTimeInstance().format(websocketTraffic.timestamp)
        operation.setText(websocketTraffic.operation.descriptionId)
        path.text = websocketTraffic.path
        host.text = websocketTraffic.host
        size.text = (websocketTraffic.contentText?.length ?: 0).formatBytes()
        ssl.visibility = if (true == websocketTraffic.ssl) View.VISIBLE else View.GONE
        itemView.setOnClickListener {
            listener(websocketTraffic.id, adapterPosition, WEBSOCKET_TRAFFIC)
        }
    }
}

@Suppress("EqualsOrHashCode")
internal class WebsocketTrafficRow(val traffic: WebsocketTraffic) : TrafficRow {
    override val id: Long = traffic.id
    override val timestamp: Long = traffic.timestamp ?: 0L
    override val type = WEBSOCKET_TRAFFIC

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebsocketTrafficRow

        if (traffic != other.traffic) return false

        return true
    }
}