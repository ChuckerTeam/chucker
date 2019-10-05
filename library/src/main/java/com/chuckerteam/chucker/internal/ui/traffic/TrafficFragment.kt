package com.chuckerteam.chucker.internal.ui.traffic

import android.content.Context
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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.TrafficType
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.ui.traffic.http.TransactionActivity
import com.chuckerteam.chucker.internal.ui.traffic.websocket.WebsocketDetailActivity

class TrafficFragment : Fragment() {
    private lateinit var tutorialView: View
    private lateinit var trafficVM: TrafficViewModel
    private lateinit var trafficAdapter: TrafficAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        trafficVM = ViewModelProviders.of(this)[TrafficViewModel::class.java]
        trafficVM.executeCurrentQuery()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.chucker_fragment_transaction_list, container, false).apply {
            tutorialView = findViewById(R.id.tutorial)
            findViewById<TextView>(R.id.link).movementMethod = LinkMovementMethod.getInstance()
            trafficAdapter = TrafficAdapter { id, _, type ->
                when (type) {
                    TrafficType.HTTP -> TransactionActivity.start(requireContext(), id)
                    TrafficType.WEBSOCKET_TRAFFIC -> WebsocketDetailActivity.start(
                        requireContext(),
                        id
                    )
                    TrafficType.WEBSOCKET_LIFECYCLE -> Unit
                }
            }
            trafficVM.networkTraffic.observe(
                this@TrafficFragment,
                Observer { list ->
                    tutorialView.visibility =
                        if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
                    trafficAdapter.submitList(list)
                }
            )
            with(findViewById<RecyclerView>(R.id.list)) {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(context, VERTICAL))
                adapter = trafficAdapter
            }
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chucker_transactions_list, menu)

        val searchMenuItem = menu.findItem(R.id.search)
        with(searchMenuItem.actionView as SearchView) {
            setIconifiedByDefault(true)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?) =
                    trafficVM.executeQuery(newText)
                override fun onQueryTextSubmit(query: String?) = true
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.clear -> {
                askForConfirmation()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun askForConfirmation() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.chucker_clear)
            .setMessage(R.string.chucker_clear_http_confirmation)
            .setPositiveButton(R.string.chucker_clear) { _, _ ->
                RepositoryProvider.websocket().deleteAllTraffic()
                RepositoryProvider.transaction().deleteAllTransactions()
                NotificationHelper.clearBuffer()
            }
            .setNegativeButton(R.string.chucker_cancel, null)
            .show()
}
