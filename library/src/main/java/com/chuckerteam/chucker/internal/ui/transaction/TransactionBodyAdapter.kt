package com.chuckerteam.chucker.internal.ui.transaction

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemBodyLineBinding

internal class TransactionBodyAdapter : RecyclerView.Adapter<TransactionBodyAdapter.BodyLineViewHolder>() {

    private val items = arrayListOf<SpannableStringBuilder>()

    fun setItems(headersItems: List<SpannableStringBuilder>) {
        items.clear()
        items.addAll(headersItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyLineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bodyItemBinding = ChuckerTransactionItemBodyLineBinding.inflate(inflater, parent, false)
        return BodyLineViewHolder(bodyItemBinding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BodyLineViewHolder, position: Int) {
        holder.bind(items[position])
    }

    internal class BodyLineViewHolder(
        private val bodyBinding: ChuckerTransactionItemBodyLineBinding
    ) : RecyclerView.ViewHolder(bodyBinding.root) {

        fun bind(item: SpannableStringBuilder) {
            bodyBinding.bodyLine.text = item
        }
    }
}
