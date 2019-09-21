package com.chuckerteam.chucker.internal.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.entity.TrafficType.HTTP

class TrafficAdapter(private val listener: TrafficClickListListener) :
    ListAdapter<TrafficRow, TrafficViewHolder>(TrafficDiffUtil()) {

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getLayout(viewType), parent, false)
        return when (viewType) {
            HTTP.ordinal -> HttpTransactionViewHolder(view, listener)
            // WEBSOCKET.ordinal -> WebsocketTrafficViewHolder(view, listener)
            else -> throw IllegalArgumentException("Unsupported row type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getLayout(viewType: Int): Int = when (viewType) {
        HTTP.ordinal -> R.layout.chucker_list_item_transaction
        // WEBSOCKET.ordinal -> R.layout.chucker_list_item_transaction
        else -> throw IllegalArgumentException("Unsupported row type: $viewType")
    }

    private class TrafficDiffUtil : DiffUtil.ItemCallback<TrafficRow>() {
        override fun areItemsTheSame(oldItem: TrafficRow, newItem: TrafficRow) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TrafficRow, newItem: TrafficRow) =
            oldItem == newItem
    }
}

abstract class TrafficViewHolder(view: View, val listener: TrafficClickListListener) :
    RecyclerView.ViewHolder(view) {
    abstract fun bind(trafficRow: TrafficRow)
}

interface TrafficRow {
    val id: Long
    val type: TrafficType
    override fun equals(other: Any?): Boolean
}

interface TrafficClickListListener {
    fun onTrafficClick(id: Long, position: Int, type: TrafficType)
}