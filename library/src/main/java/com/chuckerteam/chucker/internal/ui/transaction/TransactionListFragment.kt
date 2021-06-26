package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionListBinding
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.showDialog
import com.chuckerteam.chucker.internal.ui.MainViewModel
import kotlinx.coroutines.launch

internal class TransactionListFragment :
    Fragment(),
    SearchView.OnQueryTextListener,
    TransactionAdapter.TransactionClickListListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var transactionsBinding: ChuckerFragmentTransactionListBinding
    private lateinit var transactionsAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        transactionsBinding = ChuckerFragmentTransactionListBinding.inflate(inflater, container, false)

        transactionsAdapter = TransactionAdapter(requireContext(), this)
        with(transactionsBinding) {
            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            transactionsRecyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                adapter = transactionsAdapter
            }
        }

        return transactionsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transactions.observe(
            viewLifecycleOwner,
            Observer { transactionTuples ->
                transactionsAdapter.setData(transactionTuples)
                transactionsBinding.tutorialView.visibility =
                    if (transactionTuples.isEmpty()) View.VISIBLE else View.GONE
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chucker_transactions_list, menu)
        setUpSearch(menu)
        super.onCreateOptionsMenu(menu, inflater)
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
                requireContext().showDialog(
                    getClearDialogData(),
                    onPositiveClick = {
                        viewModel.clearTransactions()
                    },
                    onNegativeClick = null
                )
                true
            }
            R.id.export -> {
                requireContext().showDialog(
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

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(requireActivity(), transactionId)
    }

    private fun exportTransactions() = lifecycleScope.launch {
        val transactions = viewModel.getAllTransactions()
        if (transactions.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.chucker_export_empty_text, Toast.LENGTH_SHORT).show()
            return@launch
        }

        val sharableTransactions = TransactionListDetailsSharable(transactions, encodeUrls = false)
        val shareIntent = sharableTransactions.shareAsFile(
            activity = requireActivity(),
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

    companion object {
        private const val EXPORT_FILE_NAME = "transactions.txt"

        fun newInstance(): TransactionListFragment {
            return TransactionListFragment()
        }
    }
}
