package com.chuckerteam.chucker.internal.ui.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.api.Group
import com.chuckerteam.chucker.databinding.ChuckerListItemGroupBinding

internal class GroupAdapter internal constructor(
    private val onGroupItemClick: (Group) -> Unit,
) : ListAdapter<Group, GroupAdapter.ViewHolder>(GroupDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupAdapter.ViewHolder {
        val viewBinding = ChuckerListItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val itemBinding: ChuckerListItemGroupBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: Group) {
            itemView.setOnClickListener {
                onGroupItemClick.invoke(item)
            }
            itemBinding.apply {
                this.checkbox.isChecked = item.isChecked
                this.groupName.text = item.name
            }
        }
    }
}
