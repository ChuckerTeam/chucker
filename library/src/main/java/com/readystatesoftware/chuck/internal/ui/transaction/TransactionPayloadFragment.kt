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
package com.readystatesoftware.chuck.internal.ui.transaction

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.internal.data.HttpTransaction
import com.readystatesoftware.chuck.internal.support.hightlight

private const val ARG_TYPE = "type"

class TransactionPayloadFragment : Fragment(), TransactionFragment, SearchView.OnQueryTextListener {

    internal lateinit var headers: TextView
    internal lateinit var body: TextView

    private var type: Int = 0
    private var transaction: HttpTransaction? = null
    private var originalBody: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt(ARG_TYPE)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.chuck_fragment_transaction_payload, container, false)
        headers = view.findViewById<View>(R.id.headers) as TextView
        body = view.findViewById<View>(R.id.body) as TextView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (type == TYPE_RESPONSE) {
            val searchMenuItem = menu!!.findItem(R.id.search)
            searchMenuItem.isVisible = true
            val searchView = searchMenuItem.actionView as SearchView
            searchView.setOnQueryTextListener(this)
            searchView.setIconifiedByDefault(true)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun transactionUpdated(transaction: HttpTransaction) {
        this.transaction = transaction
        populateUI()
    }

    private fun populateUI() {
        if (isAdded && transaction != null) {
            when (type) {
                TYPE_REQUEST -> setText(transaction!!.getRequestHeadersString(true),
                                        transaction!!.formattedRequestBody, transaction!!.requestBodyIsPlainText())
                TYPE_RESPONSE -> setText(transaction!!.getResponseHeadersString(true),
                                         transaction!!.formattedResponseBody, transaction!!.responseBodyIsPlainText())
            }
        }
    }

    private fun setText(headersString: String, bodyString: String?, isPlainText: Boolean) {
        headers.visibility = if (TextUtils.isEmpty(headersString)) View.GONE else View.VISIBLE
        headers.text = Html.fromHtml(headersString)
        if (!isPlainText) {
            body.text = getString(R.string.chuck_body_omitted)
        } else {
            body.text = bodyString
        }
        originalBody = body.text.toString()

    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotBlank())
            body.text = originalBody?.hightlight(newText)
        else
            body.text = originalBody
        return true
    }

    companion object {

        const val TYPE_REQUEST = 0

        const val TYPE_RESPONSE = 1

        @JvmStatic
        fun newInstance(type: Int): TransactionPayloadFragment {
            return TransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, type)
                }
            }
        }
    }
}