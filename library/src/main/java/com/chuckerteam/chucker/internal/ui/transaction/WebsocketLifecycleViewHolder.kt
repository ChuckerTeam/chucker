package com.chuckerteam.chucker.internal.ui.transaction

import android.view.View
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.entity.TrafficType.WEBSOCKET_LIFECYCLE
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic

class WebsocketLifecycleViewHolder(view: View, listener: (Long, Int, TrafficType) -> Unit) :
    TrafficViewHolder(view, listener) {
    override fun bind(trafficRow: TrafficRow) {
        val websocketTraffic = (trafficRow as WebsocketTrafficRow).traffic
    }
}

@Suppress("EqualsOrHashCode")
internal class WebsocketLifecycleRow(val traffic: WebsocketTraffic) :
    TrafficRow {
    override val id: Long = traffic.id
    override val type = WEBSOCKET_LIFECYCLE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebsocketTrafficRow

        if (traffic != other.traffic) return false

        return true
    }
}