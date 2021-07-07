package com.chuckerteam.chucker.internal.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerListItemRequestTagBinding
import com.chuckerteam.chucker.internal.data.model.RequestTagFilterItem

internal class FilterItemsAdapter(
    private val onFilterSelected: (RequestTagFilterItem) -> Unit
) :
    ListAdapter<RequestTagFilterItem, FilterItemsAdapter.RequestTagViewHolder>(FilterItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestTagViewHolder =
        RequestTagViewHolder(
            ChuckerListItemRequestTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RequestTagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestTagViewHolder(private val binding: ChuckerListItemRequestTagBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var requestTag: RequestTagFilterItem? = null

        init {
            binding.root.setOnClickListener {
                requestTag?.let(onFilterSelected::invoke)
            }
        }

        fun bind(requestTag: RequestTagFilterItem) {
            binding.root.text = requestTag.name
            binding.root.isChecked = requestTag.isSelected
            this.requestTag = requestTag
        }
    }
}
