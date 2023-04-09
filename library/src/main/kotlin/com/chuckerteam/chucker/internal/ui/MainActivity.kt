package com.chuckerteam.chucker.internal.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.TransactionDetailsHarSharable
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.showDialog
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainActivity :
    BaseChuckerActivity(),
    SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mainBinding: ChuckerActivityMainBinding
    private lateinit var transactionsAdapter: TransactionAdapter

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted: Boolean ->
        if (!isPermissionGranted) {
            showToast(
                applicationContext.getString(R.string.chucker_notifications_permission_not_granted),
                Toast.LENGTH_LONG
            )
            Logger.error("Notification permission denied. Can't show transactions info")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ChuckerActivityMainBinding.inflate(layoutInflater)
        transactionsAdapter = TransactionAdapter(this) { transactionId ->
            TransactionActivity.start(this, transactionId)
        }

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName

            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            transactionsRecyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(
                        this@MainActivity,
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = transactionsAdapter
            }
        }

        viewModel.transactions.observe(
            this
        ) { transactionTuples ->
            transactionsAdapter.submitList(transactionTuples)
            mainBinding.tutorialGroup.isVisible = transactionTuples.isEmpty()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNotificationsPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNotificationsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                /* We have permission, all good */
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Snackbar.make(
                    mainBinding.root,
                    applicationContext.getString(R.string.chucker_notifications_permission_not_granted),
                    Snackbar.LENGTH_LONG
                ).setAction(applicationContext.getString(R.string.chucker_change)) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data = Uri.fromParts("package", packageName, null)
                    }.also { intent ->
                        startActivity(intent)
                    }
                }.show()
            }
            else -> {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chucker_transactions_list, menu)
        setUpSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpSearch(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                showDialog(
                    getClearDialogData(),
                    onPositiveClick = {
                        viewModel.clearTransactions()
                    },
                    onNegativeClick = null
                )
                true
            }
            R.id.share_text -> {
                showDialog(
                    getExportDialogData(R.string.chucker_export_text_http_confirmation),
                    onPositiveClick = {
                        exportTransactions(EXPORT_TXT_FILE_NAME) { transactions ->
                            TransactionListDetailsSharable(transactions, encodeUrls = false)
                        }
                    },
                    onNegativeClick = null
                )
                true
            }
            R.id.share_har -> {
                showDialog(
                    getExportDialogData(R.string.chucker_export_har_http_confirmation),
                    onPositiveClick = {
                        exportTransactions(EXPORT_HAR_FILE_NAME) { transactions ->
                            TransactionDetailsHarSharable(
                                HarUtils.harStringFromTransactions(
                                    transactions,
                                    getString(R.string.chucker_name),
                                    getString(R.string.chucker_version)
                                )
                            )
                        }
                    },
                    onNegativeClick = null
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean = true

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.updateItemsFilter(newText)
        return true
    }

    private fun exportTransactions(
        fileName: String,
        block: suspend (List<HttpTransaction>) -> Sharable
    ) {
        val applicationContext = this.applicationContext
        lifecycleScope.launch {
            val transactions = viewModel.getAllTransactions()
            if (transactions.isEmpty()) {
                showToast(applicationContext.getString(R.string.chucker_export_empty_text))
                return@launch
            }

            val sharableTransactions = block(transactions)
            val shareIntent = withContext(Dispatchers.IO) {
                sharableTransactions.shareAsFile(
                    activity = this@MainActivity,
                    fileName = fileName,
                    intentTitle = getString(R.string.chucker_share_all_transactions_title),
                    intentSubject = getString(R.string.chucker_share_all_transactions_subject),
                    clipDataLabel = "transactions"
                )
            }
            if (shareIntent != null) {
                startActivity(shareIntent)
            } else {
                showToast(applicationContext.getString(R.string.chucker_export_no_file))
            }
        }
    }

    private fun getClearDialogData(): DialogData = DialogData(
        title = getString(R.string.chucker_clear),
        message = getString(R.string.chucker_clear_http_confirmation),
        positiveButtonText = getString(R.string.chucker_clear),
        negativeButtonText = getString(R.string.chucker_cancel)
    )

    private fun getExportDialogData(@StringRes dialogMessage: Int): DialogData = DialogData(
        title = getString(R.string.chucker_export),
        message = getString(dialogMessage),
        positiveButtonText = getString(R.string.chucker_export),
        negativeButtonText = getString(R.string.chucker_cancel)
    )

    companion object {
        private const val EXPORT_TXT_FILE_NAME = "transactions.txt"
        private const val EXPORT_HAR_FILE_NAME = "transactions.har"
    }
}
