package com.chuckerteam.chucker.internal.ui.group

import androidx.recyclerview.widget.DiffUtil
import com.chuckerteam.chucker.api.Group

internal object GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }
}
