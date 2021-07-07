package com.chuckerteam.chucker.internal.ui.filter

import androidx.recyclerview.widget.DiffUtil
import com.chuckerteam.chucker.internal.data.model.RequestTagFilterItem

internal object FilterItemDiffCallback : DiffUtil.ItemCallback<RequestTagFilterItem>() {

    override fun areItemsTheSame(oldItem: RequestTagFilterItem, newItem: RequestTagFilterItem): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: RequestTagFilterItem, newItem: RequestTagFilterItem): Boolean =
        oldItem == newItem
}
