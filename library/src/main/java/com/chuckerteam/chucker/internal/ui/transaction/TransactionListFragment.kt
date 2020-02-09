package com.chuckerteam.chucker.internal.ui.transaction

import android.content.SharedPreferences
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
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.support.combineLatest
import com.chuckerteam.chucker.internal.ui.MainViewModel

internal class TransactionListFragment :
    Fragment(),
    SearchView.OnQueryTextListener,
    TransactionAdapter.TransactionClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var tutorialView: View
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        preferences = Chucker.chuckerPreferences(requireContext())
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
        val transactionsWithEncoding = viewModel.transactions.combineLatest(viewModel.encodeUrls)
        transactionsWithEncoding.observe(
            viewLifecycleOwner,
            Observer { (transactionTuples, encode) ->
                adapter.setData(transactionTuples, encode)
                tutorialView.visibility = if (transactionTuples.isEmpty()) View.VISIBLE else View.GONE
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chucker_transactions_list, menu)
        setUpSearch(menu)
        setUpUrlEncoding(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setUpSearch(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(true)
    }

    private fun setUpUrlEncoding(menu: Menu) {
        val encodeUrlsMenuItem = menu.findItem(R.id.encode_urls)
        encodeUrlsMenuItem.setOnMenuItemClickListener { item ->
            val encode = !item.isChecked
            preferences.edit().putBoolean(ENABLE_URL_ENCODING, encode).apply()
            viewModel.encodeUrls(encode)
            return@setOnMenuItemClickListener true
        }
        viewModel.encodeUrls.observe(
            viewLifecycleOwner,
            Observer { encode -> encodeUrlsMenuItem.isChecked = encode }
        )
        viewModel.encodeUrls(preferences.getBoolean(ENABLE_URL_ENCODING, false))
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

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(requireActivity(), transactionId, viewModel.encodeUrls.value == true)
    }

    companion object {
        private const val ENABLE_URL_ENCODING = "enable_url_encoding"

        fun newInstance(): TransactionListFragment {
            return TransactionListFragment()
        }
    }
}
