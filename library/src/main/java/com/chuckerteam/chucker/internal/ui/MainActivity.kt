package com.chuckerteam.chucker.internal.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.showDialog
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch

private const val EXPORT_FILE_NAME = "transactions.txt"

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
                transactionsAdapter.setData(transactionTuples)
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
            R.id.export -> {
                showDialog(
                    getExportDialogData(),
                    onPositiveClick = {
                        exportTransactions()
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

    private fun exportTransactions() = lifecycleScope.launch {
        val transactions = viewModel.getAllTransactions()
        if (transactions.isNullOrEmpty()) {
            Toast.makeText(this@MainActivity, R.string.chucker_export_empty_text, Toast.LENGTH_SHORT).show()
            return@launch
        }

        val sharableTransactions = TransactionListDetailsSharable(transactions, encodeUrls = false)
        val shareIntent = sharableTransactions.shareAsFile(
            activity = this@MainActivity,
            fileName = EXPORT_FILE_NAME,
            intentTitle = getString(R.string.chucker_share_all_transactions_title),
            intentSubject = getString(R.string.chucker_share_all_transactions_subject),
            clipDataLabel = "transactions"
        )
        if (shareIntent != null) {
            startActivity(shareIntent)
        }
    }

    private fun getClearDialogData(): DialogData = DialogData(
        title = getString(R.string.chucker_clear),
        message = getString(R.string.chucker_clear_http_confirmation),
        positiveButtonText = getString(R.string.chucker_clear),
        negativeButtonText = getString(R.string.chucker_cancel)
    )

    private fun getExportDialogData(): DialogData = DialogData(
        title = getString(R.string.chucker_export),
        message = getString(R.string.chucker_export_http_confirmation),
        positiveButtonText = getString(R.string.chucker_export),
        negativeButtonText = getString(R.string.chucker_cancel)
    )
}
