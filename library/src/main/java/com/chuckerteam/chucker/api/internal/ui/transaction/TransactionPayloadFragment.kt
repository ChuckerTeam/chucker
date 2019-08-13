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
package com.chuckerteam.chucker.api.internal.ui.transaction

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.api.internal.support.highlight

private const val ARG_TYPE = "type"

internal class TransactionPayloadFragment : Fragment(), TransactionFragment, SearchView.OnQueryTextListener {

    internal lateinit var headers: TextView
    internal lateinit var body: TextView
    internal lateinit var binaryData: ImageView

    private var type: Int = 0
    private var transaction: HttpTransaction? = null
    private var originalBody: String? = null
    private var uiLoaderTask: UiLoaderTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt(ARG_TYPE)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chucker_fragment_transaction_payload, container, false)
        headers = view.findViewById<TextView>(R.id.headers)
        body = view.findViewById<TextView>(R.id.body)
        binaryData = view.findViewById<ImageView>(R.id.image)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiLoaderTask?.cancel(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (type == TYPE_RESPONSE) {
            val searchMenuItem = menu.findItem(R.id.search)
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
            UiLoaderTask(this).execute(Pair(type, transaction!!))
        }
    }

    private fun setBody(headersString: String, bodyString: String?, isPlainText: Boolean, image: Bitmap?) {
        headers.visibility = if (TextUtils.isEmpty(headersString)) View.GONE else View.VISIBLE
        headers.text = Html.fromHtml(headersString)
        val isImageData = image != null
        if (!isPlainText && !isImageData) {
            body.text = context?.getString(R.string.chucker_body_omitted)
        } else if (!isImageData) {
            body.text = bodyString
        }
        if (image != null) {
            binaryData.visibility = View.VISIBLE
            binaryData.setImageBitmap(image)
        } else {
            binaryData.visibility = View.GONE
        }
        originalBody = body.text.toString()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotBlank())
            body.text = originalBody?.highlight(newText)
        else
            body.text = originalBody
        return true
    }

    private class UiLoaderTask(val fragment: TransactionPayloadFragment) :
        AsyncTask<Pair<Int, HttpTransaction>, Unit, UiPayload>() {

        override fun doInBackground(vararg params: Pair<Int, HttpTransaction>):
        UiPayload {
            val (type, transaction) = params[0]
            return when (type) {
                TYPE_REQUEST -> UiPayload(
                    transaction.getRequestHeadersString(true),
                    transaction.getFormattedRequestBody(),
                    transaction.isRequestBodyPlainText
                )
                else -> UiPayload(
                    transaction.getResponseHeadersString(true),
                    transaction.getFormattedResponseBody(),
                    transaction.isResponseBodyPlainText,
                    transaction.responseImageBitmap
                )
            }
        }

        override fun onPostExecute(result: UiPayload) {
            with(result) {
                fragment.setBody(headersString, bodyString, isPlainText, image)
            }
        }
    }

    private data class UiPayload(
        val headersString: String,
        val bodyString: String?,
        val isPlainText: Boolean,
        val image: Bitmap? = null
    )

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
