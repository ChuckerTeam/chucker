package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerListItemTransactionBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.support.TransactionDiffCallback
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonSyntaxException
import java.io.StringReader
import java.lang.reflect.Type
import java.text.DateFormat
import javax.net.ssl.HttpsURLConnection

internal class TransactionAdapter internal constructor(
        context: Context,
        private val onTransactionClick: (Long) -> Unit,
) : ListAdapter<HttpTransactionTuple, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback) {

    private val colorDefault: Int = ContextCompat.getColor(context, R.color.chucker_status_default)
    private val colorRequested: Int = ContextCompat.getColor(context, R.color.chucker_status_requested)
    private val colorError: Int = ContextCompat.getColor(context, R.color.chucker_status_error)
    private val color500: Int = ContextCompat.getColor(context, R.color.chucker_status_500)
    private val color400: Int = ContextCompat.getColor(context, R.color.chucker_status_400)
    private val color300: Int = ContextCompat.getColor(context, R.color.chucker_status_300)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val viewBinding = ChuckerListItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) =
        holder.bind(getItem(position))

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

        @Throws(JsonSyntaxException::class)
        fun <T> fromJson(json: String?, typeOfT: Type?): T? {
            if (json == null) {
                return null
            }
            val reader = StringReader(json)
            return Gson().fromJson<Any>(reader, typeOfT) as T
        }

        @SuppressLint("SetTextI18n")
        fun bind(transaction: HttpTransactionTuple) {
            transactionId = transaction.id

            itemBinding.apply {
                path.text = "${transaction.method} ${transaction.getFormattedPath(encode = false)}"
                host.text = transaction.host
                timeStart.text = DateFormat.getTimeInstance().format(transaction.requestDate)
                val request = transaction.requestBody?.replace("\n", "")?.replace(" ", "") ?: ""

                val queryObject = try {
                    val a: JsonArray? = fromJson(transaction.requestBody, JsonArray::class.java)
                    a?.let {
                        it.asJsonArray[0]?.asJsonObject?.get("query").toString()
                    } ?: ""
                } catch (e: Throwable) {
                    ""
                }

                queryName.text = if (request.isEmpty()) {
                    "Request is Empty"
                } else {
                    if (queryObject.isEmpty()) {
                        request
                    } else {
                        queryObject
                    }
                }

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
