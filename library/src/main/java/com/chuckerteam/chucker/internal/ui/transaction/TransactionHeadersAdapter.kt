package com.chuckerteam.chucker.internal.ui.transaction

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemHeadersBinding
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.support.applyBoldSpan

internal class TransactionHeadersAdapter : RecyclerView.Adapter<TransactionHeadersAdapter.HeaderViewHolder>() {

    private val headers = arrayListOf<HttpHeader>()

    fun setItems(headersItems: List<HttpHeader>) {
        headers.clear()
        headers.addAll(headersItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val headersItemBinding = ChuckerTransactionItemHeadersBinding.inflate(inflater, parent, false)
        return HeaderViewHolder(headersItemBinding)
    }

    override fun getItemCount(): Int = headers.size

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(headers[position])
    }

    internal class HeaderViewHolder(
        private val headerBinding: ChuckerTransactionItemHeadersBinding
    ) : RecyclerView.ViewHolder(headerBinding.root) {

        fun bind(headerItem: HttpHeader) {
            val headerName = "${headerItem.name}: ".applyBoldSpan()
            val headerValue = headerItem.value
            headerBinding.responseHeaders.text = SpannableStringBuilder(headerName).append(headerValue)
        }
    }
}