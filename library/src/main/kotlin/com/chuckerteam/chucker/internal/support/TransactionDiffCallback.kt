package com.chuckerteam.chucker.internal.support

import androidx.recyclerview.widget.DiffUtil
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.Transaction

internal object TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }

    // Overriding function is empty on purpose to avoid flickering by default animator
    override fun getChangePayload(oldItem: Transaction, newItem: Transaction) = Unit
}
