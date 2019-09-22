package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper

class TrafficFragment : Fragment() {
    private lateinit var dataSource: LiveData<List<HttpTransactionTuple>>
    private lateinit var tutorialView : View
    private var currentFilter: String? = ""
    private val trafficAdapter = TrafficAdapter { id, _, _ ->
        TransactionActivity.start(activity, id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.chucker_fragment_transaction_list, container, false).apply {
        with(findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, VERTICAL))
            adapter = trafficAdapter
        }
        tutorialView = findViewById(R.id.tutorial)
        findViewById<TextView>(R.id.link).movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataSource = getDataSource(currentFilter).andListen()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chucker_transactions_list, menu)

        val searchMenuItem = menu.findItem(R.id.search)
        with(searchMenuItem.actionView as SearchView) {
            setIconifiedByDefault(true)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?) = executeQuery(newText)
                override fun onQueryTextSubmit(query: String?) = true
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.clear -> {
            askForConfirmation()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun askForConfirmation() = AlertDialog.Builder(requireContext())
        .setTitle(R.string.chucker_clear)
        .setMessage(R.string.chucker_clear_http_confirmation)
        .setPositiveButton(R.string.chucker_clear) { _, _ ->
            RepositoryProvider.websocket().deleteAllTraffic()
            RepositoryProvider.transaction().deleteAllTransactions()
            NotificationHelper.clearBuffer()
        }
        .setNegativeButton(R.string.chucker_cancel, null)
        .show()

    private fun executeQuery(newText: String?): Boolean {
        currentFilter = newText
        dataSource.stopListening()
        dataSource = getDataSource(currentFilter).andListen()
        return true
    }

    private fun getDataSource(currentFilter: String?): LiveData<List<HttpTransactionTuple>> {
        val repository = RepositoryProvider.transaction()
        return when {
            currentFilter.isNullOrEmpty() -> repository.getSortedTransactionTuples()
            currentFilter.isDigitsOnly() -> repository.getFilteredTransactionTuples(
                currentFilter,
                ""
            )
            else -> repository.getFilteredTransactionTuples("", currentFilter)
        }
    }

    private fun String.isDigitsOnly(): Boolean = TextUtils.isDigitsOnly(this)

    private fun LiveData<List<HttpTransactionTuple>>.stopListening() =
        removeObservers(this@TrafficFragment)

    private fun LiveData<List<HttpTransactionTuple>>.andListen(): LiveData<List<HttpTransactionTuple>> {
        this.observe(this@TrafficFragment, Observer { tuples ->
            tutorialView.visibility = if (tuples.isNullOrEmpty()) View.VISIBLE else View.GONE
            trafficAdapter.submitList(tuples.map { HttpTrafficRow(it) })
        })
        return this
    }
}