package com.chuckerteam.chucker.internal.ui.transaction

import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemHeadersBinding

internal class TransactionHeadersAdapter : RecyclerView.Adapter<TransactionHeadersAdapter.HeaderViewHolder>() {

    private lateinit var headers: Spanned

    fun setItems(headersItems: Spanned) {
        headers = headersItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val headersItemBinding = ChuckerTransactionItemHeadersBinding.inflate(inflater, parent, false)
        return HeaderViewHolder(headersItemBinding)
    }

    override fun getItemCount(): Int = if (!this::headers.isInitialized) 0 else 1

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(headers)
    }

    internal class HeaderViewHolder(
        private val headerBinding: ChuckerTransactionItemHeadersBinding
    ) : RecyclerView.ViewHolder(headerBinding.root) {

        fun bind(headerItem: Spanned) {
            headerBinding.responseHeaders.text = headerItem
        }
    }
}