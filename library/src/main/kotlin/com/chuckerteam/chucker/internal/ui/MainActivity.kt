package com.chuckerteam.chucker.internal.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.TransactionDetailsHarSharable
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.showDialog
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
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
                addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
                adapter = transactionsAdapter
            }
        }

        viewModel.transactions.observe(
            this,
            { transactionTuples ->
                transactionsAdapter.submitList(transactionTuples)
                mainBinding.tutorialGroup.isVisible = transactionTuples.isEmpty()
            }
        )
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

    private fun exportTransactions(fileName: String, block: suspend (List<HttpTransaction>) -> Sharable) {
        val applicationContext = this.applicationContext
        lifecycleScope.launch {
            val transactions = viewModel.getAllTransactions()
            if (transactions.isNullOrEmpty()) {
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
                Toast.makeText(applicationContext, R.string.chucker_export_no_file, Toast.LENGTH_SHORT).show()
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
