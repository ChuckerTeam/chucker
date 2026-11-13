package com.chuckerteam.chucker.internal.ui.mapremote

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerListItemMapRemoteRuleBinding
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule

internal class MapRemoteRuleAdapter(
    private val onRuleClick: (MapRemoteRule) -> Unit,
) : ListAdapter<MapRemoteRule, MapRemoteRuleAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            ChuckerListItemMapRemoteRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

    inner class ViewHolder(
        private val binding: ChuckerListItemMapRemoteRuleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(rule: MapRemoteRule) {
            binding.requestUrl.text = "${rule.requestMethod} ${rule.requestUrl}"
            binding.targetUrl.text = "→ ${rule.targetUrl}"
            binding.root.setOnClickListener { onRuleClick(rule) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MapRemoteRule>() {
        override fun areItemsTheSame(
            oldItem: MapRemoteRule,
            newItem: MapRemoteRule,
        ) = oldItem.requestUrl == newItem.requestUrl && oldItem.requestMethod == newItem.requestMethod

        override fun areContentsTheSame(
            oldItem: MapRemoteRule,
            newItem: MapRemoteRule,
        ) = oldItem == newItem
    }
}
