package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionListBinding
import com.chuckerteam.chucker.internal.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class TransactionListFragment :
    Fragment(),
    SearchView.OnQueryTextListener,
    TransactionAdapter.TransactionClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var transactionsBinding: ChuckerFragmentTransactionListBinding
    private lateinit var transactionsAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        return if (item.itemId == R.id.clear) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chucker_clear)
                .setMessage(R.string.chucker_clear_http_confirmation)
                .setPositiveButton(
                    R.string.chucker_clear
                ) { _, _ ->
                    viewModel.clearTransactions()
                }
                .setNegativeButton(R.string.chucker_cancel, null)
                .show()
            true
        } else {
            super.onOptionsItemSelected(item)
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

    companion object {
        fun newInstance(): TransactionListFragment {
            return TransactionListFragment()
        }
    }
}
