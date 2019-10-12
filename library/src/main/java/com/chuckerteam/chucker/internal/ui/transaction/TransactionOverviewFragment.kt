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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

internal class TransactionOverviewFragment : Fragment(), TransactionFragment {
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

    private var transaction: HttpTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.chucker_fragment_transaction_overview, container, false).apply {
            url = findViewById(R.id.url)
            method = findViewById(R.id.method)
            protocol = findViewById(R.id.protocol)
            status = findViewById(R.id.status)
            response = findViewById(R.id.response)
            ssl = findViewById(R.id.ssl)
            requestTime = findViewById(R.id.request_time)
            responseTime = findViewById(R.id.response_time)
            duration = findViewById(R.id.duration)
            requestSize = findViewById(R.id.request_size)
            responseSize = findViewById(R.id.response_size)
            totalSize = findViewById(R.id.total_size)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val saveMenuItem = menu.findItem(R.id.save_body)
        saveMenuItem.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun transactionUpdated(transaction: HttpTransaction) {
        this.transaction = transaction
        populateUI()
    }

    private fun populateUI() {
        if (isAdded) {
            transaction?.let { transaction ->
                url.text = transaction.url
                method.text = transaction.method
                protocol.text = transaction.protocol
                status.text = transaction.status.toString()
                response.text = transaction.responseSummaryText
                ssl.setText(if (transaction.isSsl) R.string.chucker_yes else R.string.chucker_no)
                requestTime.text = transaction.requestDateString
                responseTime.text = transaction.responseDateString
                duration.text = transaction.durationString
                requestSize.text = transaction.requestSizeString
                responseSize.text = transaction.responseSizeString
                totalSize.text = transaction.totalSizeString
            }
        }
    }
}
