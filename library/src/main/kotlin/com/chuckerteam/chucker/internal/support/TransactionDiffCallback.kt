package com.chuckerteam.chucker.internal.support

import androidx.recyclerview.widget.DiffUtil
import com.chuckerteam.chucker.internal.ui.TransactionUi

internal object TransactionDiffCallback : DiffUtil.ItemCallback<TransactionUi>() {
    override fun areItemsTheSame(
        oldItem: TransactionUi,
        newItem: TransactionUi,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: TransactionUi,
        newItem: TransactionUi,
    ): Boolean = oldItem == newItem

    // Overriding function is empty on purpose to avoid flickering by default animator
    override fun getChangePayload(
        oldItem: TransactionUi,
        newItem: TransactionUi,
    ) = Unit
}
