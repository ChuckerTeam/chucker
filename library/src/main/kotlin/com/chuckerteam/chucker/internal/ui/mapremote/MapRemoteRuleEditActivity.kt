package com.chuckerteam.chucker.internal.ui.mapremote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityMapRemoteRuleEditBinding
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import com.chuckerteam.chucker.internal.data.entity.toMapRemoteRuleUrl
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal class MapRemoteRuleEditActivity : BaseChuckerActivity() {
    private val viewModel: MapRemoteRuleViewModel by viewModels()

    private lateinit var binding: ChuckerActivityMapRemoteRuleEditBinding
    private lateinit var requestMethod: String
    private var existingRule: MapRemoteRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestUrl = intent.getStringExtra(EXTRA_REQUEST_URL)?.toHttpUrlOrNull()?.toMapRemoteRuleUrl()
        val method = intent.getStringExtra(EXTRA_REQUEST_METHOD)
        if (requestUrl == null || method == null) {
            finish()
            return
        }
        requestMethod = method

        binding = ChuckerActivityMapRemoteRuleEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.root, binding.appBarLayout)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.method.text = requestMethod
        if (savedInstanceState == null) {
            binding.requestUrlInput.setText(requestUrl)
        }
        binding.save.setOnClickListener { save() }
        // A network interceptor can't change the host of a call, so a rule made here wouldn't be applied.
        val isNetworkInterceptor = intent.getBooleanExtra(EXTRA_NETWORK_INTERCEPTOR, false)
        binding.networkInterceptorNotice.isVisible = isNetworkInterceptor
        binding.save.isEnabled = !isNetworkInterceptor

        viewModel.getRule(requestUrl, requestMethod).observe(this) { rule ->
            onRuleLoaded(rule, restoreInput = savedInstanceState == null)
        }
        viewModel.invalidRequestUrl.observe(this) { showRequestUrlError() }
        viewModel.ruleAlreadyExists.observe(this) { showRequestUrlError() }
        viewModel.invalidTargetUrl.observe(this) { invalid ->
            binding.targetUrlLayout.error = urlError(invalid)
        }
        viewModel.ruleSaved.observe(this) { closeWith(R.string.chucker_map_remote_saved) }
        viewModel.ruleDeleted.observe(this) { closeWith(R.string.chucker_map_remote_deleted) }
        viewModel.databaseFailed.observe(this) { showToast(getString(R.string.chucker_map_remote_failed)) }
    }

    private fun onRuleLoaded(
        rule: MapRemoteRule?,
        restoreInput: Boolean,
    ) {
        existingRule = rule ?: return
        binding.requestUrlLayout.isEnabled = false
        if (restoreInput) {
            binding.targetUrlInput.setText(rule.targetUrl)
        }
        invalidateOptionsMenu()
    }

    private fun save() {
        viewModel.saveRule(
            binding.requestUrlInput.text.toString(),
            requestMethod,
            binding.targetUrlInput.text.toString(),
            canReplace = existingRule != null,
        )
    }

    private fun showRequestUrlError() {
        binding.requestUrlLayout.error =
            when {
                viewModel.invalidRequestUrl.value == true -> getString(R.string.chucker_map_remote_invalid_url)
                viewModel.ruleAlreadyExists.value == true -> getString(R.string.chucker_map_remote_already_exists)
                else -> null
            }
    }

    private fun urlError(invalid: Boolean) = getString(R.string.chucker_map_remote_invalid_url).takeIf { invalid }

    private fun closeWith(message: Int) {
        showToast(getString(message))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chucker_map_remote_rule_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.delete_rule).isVisible = existingRule != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == R.id.delete_rule) {
            existingRule?.let(viewModel::deleteRule)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_REQUEST_URL = "request_url"
        private const val EXTRA_REQUEST_METHOD = "request_method"
        private const val EXTRA_NETWORK_INTERCEPTOR = "network_interceptor"

        fun start(
            context: Context,
            requestUrl: String,
            requestMethod: String,
            isNetworkInterceptor: Boolean = false,
        ) {
            context.startActivity(createIntent(context, requestUrl, requestMethod, isNetworkInterceptor))
        }

        fun createIntent(
            context: Context,
            requestUrl: String,
            requestMethod: String,
            isNetworkInterceptor: Boolean = false,
        ): Intent =
            Intent(context, MapRemoteRuleEditActivity::class.java)
                .putExtra(EXTRA_REQUEST_URL, requestUrl)
                .putExtra(EXTRA_REQUEST_METHOD, requestMethod)
                .putExtra(EXTRA_NETWORK_INTERCEPTOR, isNetworkInterceptor)
    }
}
