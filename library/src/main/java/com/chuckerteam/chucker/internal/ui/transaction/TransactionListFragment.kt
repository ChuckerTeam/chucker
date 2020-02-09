package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.ui.MainViewModel

internal class TransactionListFragment :
    Fragment(),
    SearchView.OnQueryTextListener,
    TransactionAdapter.TransactionClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var tutorialView: View

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
        val view = inflater.inflate(R.layout.chucker_fragment_transaction_list, container, false)
        tutorialView = view.findViewById(R.id.tutorial)
        view.findViewById<TextView>(R.id.link).movementMethod = LinkMovementMethod.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val context = view.context
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        adapter = TransactionAdapter(context, this)
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transactions.observe(
            viewLifecycleOwner,
            Observer { transactionTuples ->
                adapter.setData(transactionTuples)
                tutorialView.visibility = if (transactionTuples.isEmpty()) View.VISIBLE else View.GONE
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chucker_transactions_list, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.clear) {
            AlertDialog.Builder(requireContext())
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

    override fun onTransactionClick(transactionId: Long, position: Int) =
        TransactionActivity.start(requireActivity(), transactionId)

    companion object {
        fun newInstance(): TransactionListFragment {
            return TransactionListFragment()
        }
    }
}
