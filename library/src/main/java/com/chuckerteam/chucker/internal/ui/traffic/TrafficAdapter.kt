package com.chuckerteam.chucker.internal.ui.traffic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.ui.traffic.http.HttpTransactionViewHolder
import com.chuckerteam.chucker.internal.ui.traffic.websocket.WebsocketLifecycleViewHolder
import com.chuckerteam.chucker.internal.ui.traffic.websocket.WebsocketTrafficViewHolder

class TrafficAdapter(private val listener: (Long, Int, TrafficType) -> Unit) :
    RecyclerView.Adapter<TrafficViewHolder>() {

    private var items: List<TrafficRow> = emptyList()

    fun submitList(list: List<TrafficRow>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].type.ordinal

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) =
        holder.bind(items[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getLayout(viewType), parent, false)
        return when (viewType) {
            TrafficType.HTTP.ordinal -> HttpTransactionViewHolder(view, listener)
            TrafficType.WEBSOCKET_LIFECYCLE.ordinal -> WebsocketLifecycleViewHolder(view, listener)
            TrafficType.WEBSOCKET_TRAFFIC.ordinal -> WebsocketTrafficViewHolder(view, listener)
            else -> throw IllegalArgumentException("Unsupported row type: $viewType")
        }
    }

    private fun getLayout(viewType: Int): Int = when (viewType) {
        TrafficType.HTTP.ordinal -> R.layout.chucker_list_item_transaction
        TrafficType.WEBSOCKET_LIFECYCLE.ordinal -> R.layout.chucker_list_item_websocket_lifecycle
        TrafficType.WEBSOCKET_TRAFFIC.ordinal -> R.layout.chucker_list_item_websocket_data
        else -> throw IllegalArgumentException("Unsupported row type: $viewType")
    }
}

abstract class TrafficViewHolder(view: View, val listener: (Long, Int, TrafficType) -> Unit) :
    RecyclerView.ViewHolder(view) {
    abstract fun bind(trafficRow: TrafficRow)
}

interface TrafficRow {
    val id: Long
    val timestamp: Long
    val type: TrafficType
}
