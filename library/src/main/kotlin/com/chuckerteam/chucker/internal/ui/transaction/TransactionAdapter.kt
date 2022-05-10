package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerListItemTransactionEventBinding
import com.chuckerteam.chucker.databinding.ChuckerListItemTransactionHttpBinding
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.support.TransactionDiffCallback
import java.text.DateFormat
import javax.net.ssl.HttpsURLConnection

internal class TransactionAdapter internal constructor(
    context: Context,
    private val onTransactionClick: (Long) -> Unit,
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback) {

    private val colorDefault: Int = ContextCompat.getColor(context, R.color.chucker_status_default)
    private val colorRequested: Int =
        ContextCompat.getColor(context, R.color.chucker_status_requested)
    private val colorError: Int = ContextCompat.getColor(context, R.color.chucker_status_error)
    private val color500: Int = ContextCompat.getColor(context, R.color.chucker_status_500)
    private val color400: Int = ContextCompat.getColor(context, R.color.chucker_status_400)
    private val color300: Int = ContextCompat.getColor(context, R.color.chucker_status_300)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return when (viewType) {
            VIEW_TYPE_EVENT -> {
                val viewBinding = ChuckerListItemTransactionEventBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EventTransactionViewHolder(viewBinding)
            }
            else -> {
                val viewBinding = ChuckerListItemTransactionHttpBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HttpTransactionViewHolder(viewBinding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HttpTransaction, is HttpTransactionTuple -> VIEW_TYPE_HTTP
            is EventTransaction -> VIEW_TYPE_EVENT
            else -> 0
        }
    }

    override fun onBindViewHolder(viewHolder: TransactionViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }


    inner class EventTransactionViewHolder(
        private val itemBinding: ChuckerListItemTransactionEventBinding
    ) : TransactionAdapter.TransactionViewHolder(itemBinding.root) {
        private var transactionId: Long? = null

        init {
            itemView.setOnClickListener {
                transactionId?.let {
                    onTransactionClick.invoke(it)
                }
            }
        }

        override fun bind(transaction: Transaction) {
            transactionId = transaction.id

            transaction as EventTransaction

            itemBinding.apply {
                title.text = transaction.title
                payload.text = transaction.payload
                timeStart.text = DateFormat.getTimeInstance().format(transaction.receivedDate)
            }
        }

    }

    inner class HttpTransactionViewHolder(
        private val itemBinding: ChuckerListItemTransactionHttpBinding
    ) : TransactionAdapter.TransactionViewHolder(itemBinding.root) {

        private var transactionId: Long? = null

        init {
            itemView.setOnClickListener {
                transactionId?.let {
                    onTransactionClick.invoke(it)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun bind(transaction: Transaction) {
            transactionId = transaction.id

            transaction as HttpTransactionTuple

            itemBinding.apply {
                path.text =
                    "${transaction.method} ${transaction.getFormattedPath(encode = false)}"
                host.text = transaction.host
                timeStart.text = DateFormat.getTimeInstance().format(transaction.requestDate)

                setProtocolImage(if (transaction.isSsl) ProtocolResources.Https() else ProtocolResources.Http())

                if (transaction.status === HttpTransaction.Status.Complete) {
                    code.text = transaction.responseCode.toString()
                    duration.text = transaction.durationString
                    size.text = transaction.totalSizeString
                } else {
                    code.text = ""
                    duration.text = ""
                    size.text = ""
                }
                if (transaction.status === HttpTransaction.Status.Failed) {
                    code.text = "!!!"
                }
            }

            setStatusColor(transaction)
        }

        private fun setProtocolImage(resources: ProtocolResources) {
            itemBinding.ssl.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    resources.icon
                )
            )
            ImageViewCompat.setImageTintList(
                itemBinding.ssl,
                ColorStateList.valueOf(ContextCompat.getColor(itemView.context, resources.color))
            )
        }

        private fun setStatusColor(transaction: Transaction) {
            if (transaction is HttpTransactionTuple) {
                val color: Int = when {
                    (transaction.status === HttpTransaction.Status.Failed) -> colorError
                    (transaction.status === HttpTransaction.Status.Requested) -> colorRequested
                    (transaction.responseCode == null) -> colorDefault
                    (transaction.responseCode!! >= HttpsURLConnection.HTTP_INTERNAL_ERROR) -> color500
                    (transaction.responseCode!! >= HttpsURLConnection.HTTP_BAD_REQUEST) -> color400
                    (transaction.responseCode!! >= HttpsURLConnection.HTTP_MULT_CHOICE) -> color300
                    else -> colorDefault
                }
                itemBinding.code.setTextColor(color)
                itemBinding.path.setTextColor(color)
            }
        }
    }

    abstract class TransactionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        abstract fun bind(transaction: Transaction)
    }
}

private const val VIEW_TYPE_HTTP = 1
private const val VIEW_TYPE_EVENT = 2
