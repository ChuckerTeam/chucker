/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper

internal class TransactionListFragment :
    Fragment(),
    SearchView.OnQueryTextListener,
    TransactionAdapter.TransactionClickListListener,
    Observer<List<HttpTransactionTuple>> {

    private var currentFilter = ""
    private lateinit var adapter: TransactionAdapter
    private lateinit var dataSource: LiveData<List<HttpTransactionTuple>>
    private lateinit var tutorialView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataSource = getDataSource(currentFilter)
        dataSource.observe(this, this)
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
                    RepositoryProvider.transaction().deleteAllTransactions()
                    NotificationHelper.clearBuffer()
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
        currentFilter = newText
        dataSource.removeObservers(this)
        dataSource = getDataSource(currentFilter)
        dataSource.observe(this, this)
        return true
    }

    private fun getDataSource(currentFilter: String): LiveData<List<HttpTransactionTuple>> = when {
        currentFilter.isEmpty() ->
            RepositoryProvider.transaction().getSortedTransactionTuples()
        TextUtils.isDigitsOnly(currentFilter) ->
            RepositoryProvider.transaction().getFilteredTransactionTuples(currentFilter, "")
        else ->
            RepositoryProvider.transaction().getFilteredTransactionTuples("", currentFilter)
    }

    override fun onChanged(tuples: List<HttpTransactionTuple>) {
        adapter.setData(tuples)
        tutorialView.visibility = if (tuples.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onTransactionClick(transactionId: Long, position: Int) =
        TransactionActivity.start(requireActivity(), transactionId)

    companion object {
        fun newInstance(): TransactionListFragment {
            return TransactionListFragment()
        }
    }
}
