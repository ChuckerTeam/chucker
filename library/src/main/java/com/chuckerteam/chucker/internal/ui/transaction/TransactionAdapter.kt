package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerListItemTransactionBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import java.text.DateFormat
import javax.net.ssl.HttpsURLConnection

internal class TransactionAdapter internal constructor(
    context: Context,
    private val onTransactionClick: (Long) -> Unit,
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private var transactions: List<HttpTransactionTuple> = arrayListOf()

    private val colorDefault: Int = ContextCompat.getColor(context, R.color.chucker_status_default)
    private val colorRequested: Int = ContextCompat.getColor(context, R.color.chucker_status_requested)
    private val colorError: Int = ContextCompat.getColor(context, R.color.chucker_status_error)
    private val color500: Int = ContextCompat.getColor(context, R.color.chucker_status_500)
    private val color400: Int = ContextCompat.getColor(context, R.color.chucker_status_400)
    private val color300: Int = ContextCompat.getColor(context, R.color.chucker_status_300)

    override fun getItemCount(): Int = transactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val viewBinding = ChuckerListItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) =
        holder.bind(transactions[position])

    fun setData(httpTransactions: List<HttpTransactionTuple>) {
        this.transactions = httpTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val itemBinding: ChuckerListItemTransactionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        private var transactionId: Long? = null

        init {
            itemView.setOnClickListener {
                transactionId?.let {
                    onTransactionClick.invoke(it)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(transaction: HttpTransactionTuple) {
            transactionId = transaction.id

            itemBinding.apply {
                path.text = "${transaction.method} ${transaction.getFormattedPath(encode = false)}"
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
            itemBinding.ssl.setImageDrawable(AppCompatResources.getDrawable(itemView.context, resources.icon))
            ImageViewCompat.setImageTintList(
                itemBinding.ssl,
                ColorStateList.valueOf(ContextCompat.getColor(itemView.context, resources.color))
            )
        }

        private fun setStatusColor(transaction: HttpTransactionTuple) {
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
