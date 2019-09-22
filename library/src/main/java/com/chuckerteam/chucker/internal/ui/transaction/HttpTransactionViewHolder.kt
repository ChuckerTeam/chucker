package com.chuckerteam.chucker.internal.ui.transaction

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction.Status.Failed
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction.Status.Requested
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.entity.TrafficType.HTTP
import java.text.DateFormat

class HttpTransactionViewHolder(view: View, listener: (Long, Int, TrafficType) -> Unit) :
    TrafficViewHolder(view, listener) {
    private val code = view.findViewById<TextView>(R.id.chucker_code)
    private val path = view.findViewById<TextView>(R.id.chucker_path)
    private val host = view.findViewById<TextView>(R.id.chucker_host)
    private val start = view.findViewById<TextView>(R.id.chucker_time_start)
    private val duration = view.findViewById<TextView>(R.id.chucker_duration)
    private val size = view.findViewById<TextView>(R.id.chucker_size)
    private val ssl = view.findViewById<ImageView>(R.id.chucker_ssl)

    override fun bind(trafficRow: TrafficRow) {
        val transaction = (trafficRow as HttpTrafficRow).transaction
        path.text = String.format("%s %s", transaction.method, transaction.path)
        host.text = transaction.host
        start.text = DateFormat.getTimeInstance().format(transaction.requestDate)
        ssl.visibility = if (transaction.isSsl) View.VISIBLE else View.GONE
        if (transaction.status === HttpTransaction.Status.Complete) {
            code.text = transaction.responseCode.toString()
            duration.text = transaction.durationString
            size.text = transaction.totalSizeString
        } else {
            code.text = ""
            duration.text = ""
            size.text = ""
        }
        if (transaction.status === Failed) {
            code.text = "!!!"
        }
        with(ContextCompat.getColor(itemView.context, trafficRow.statusColor)) {
            code.setTextColor(this)
            path.setTextColor(this)
        }
        itemView.setOnClickListener {
            listener(transaction.id, adapterPosition, HTTP)
        }
    }
}

@Suppress("EqualsOrHashCode")
internal class HttpTrafficRow(val transaction: HttpTransactionTuple) :
    TrafficRow {
    override val id: Long = transaction.id
    override val type = HTTP

    val statusColor: Int = when {
        transaction.status === Failed -> R.color.chucker_status_error
        transaction.status === Requested -> R.color.chucker_status_requested
        transaction.responseCode == null -> R.color.chucker_status_default
        transaction.responseCode!! >= 500 -> R.color.chucker_status_500
        transaction.responseCode!! >= 400 -> R.color.chucker_status_400
        transaction.responseCode!! >= 300 -> R.color.chucker_status_300
        else -> R.color.chucker_status_default
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpTrafficRow

        if (transaction != other.transaction) return false

        return true
    }
}