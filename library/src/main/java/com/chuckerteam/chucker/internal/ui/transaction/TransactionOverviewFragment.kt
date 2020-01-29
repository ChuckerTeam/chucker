package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.chuckerteam.chucker.R

internal class TransactionOverviewFragment : Fragment() {

    private lateinit var url: TextView
    private lateinit var method: TextView
    private lateinit var protocol: TextView
    private lateinit var status: TextView
    private lateinit var response: TextView
    private lateinit var ssl: TextView
    private lateinit var requestTime: TextView
    private lateinit var responseTime: TextView
    private lateinit var duration: TextView
    private lateinit var requestSize: TextView
    private lateinit var responseSize: TextView
    private lateinit var totalSize: TextView
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(requireActivity())[TransactionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_transaction_overview, container, false)
            .also {
                url = it.findViewById(R.id.url)
                method = it.findViewById(R.id.method)
                protocol = it.findViewById(R.id.protocol)
                status = it.findViewById(R.id.status)
                response = it.findViewById(R.id.response)
                ssl = it.findViewById(R.id.ssl)
                requestTime = it.findViewById(R.id.request_time)
                responseTime = it.findViewById(R.id.response_time)
                duration = it.findViewById(R.id.duration)
                requestSize = it.findViewById(R.id.request_size)
                responseSize = it.findViewById(R.id.response_size)
                totalSize = it.findViewById(R.id.total_size)
            }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val saveMenuItem = menu.findItem(R.id.save_body)
        saveMenuItem.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transaction.observe(
            this,
            Observer { transaction ->
                url.text = transaction.url
                method.text = transaction.method
                protocol.text = transaction.protocol
                status.text = transaction.status.toString()
                response.text = transaction.responseSummaryText
                ssl.setText(if (transaction.isSsl) R.string.yes else R.string.no)
                requestTime.text = transaction.requestDateString
                responseTime.text = transaction.responseDateString
                duration.text = transaction.durationString
                requestSize.text = transaction.requestSizeString
                responseSize.text = transaction.responseSizeString
                totalSize.text = transaction.totalSizeString
            }
        )
    }
}
