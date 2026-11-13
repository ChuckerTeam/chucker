package com.chuckerteam.chucker.internal.ui.mapremote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.databinding.ChuckerActivityMapRemoteRulesBinding
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

internal class MapRemoteRulesActivity : BaseChuckerActivity() {
    private val viewModel: MapRemoteRuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ChuckerActivityMapRemoteRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.root, binding.appBarLayout)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter =
            MapRemoteRuleAdapter { rule ->
                MapRemoteRuleEditActivity.start(this, rule.requestUrl, rule.requestMethod)
            }
        binding.rulesRecyclerView.adapter = adapter
        binding.rulesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        viewModel.rules.observe(this) { rules ->
            adapter.submitList(rules)
            binding.emptyText.isVisible = rules.isEmpty()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MapRemoteRulesActivity::class.java))
        }
    }
}
