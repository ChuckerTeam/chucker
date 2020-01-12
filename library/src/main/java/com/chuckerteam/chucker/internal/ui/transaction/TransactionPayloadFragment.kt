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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val GET_FILE_FOR_SAVING_REQUEST_CODE: Int = 43

internal class TransactionPayloadFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private lateinit var progressLoading: ProgressBar
    private lateinit var transactionContentList: RecyclerView

    private var backgroundSpanColor: Int = Color.YELLOW
    private var foregroundSpanColor: Int = Color.RED

    private var type: Int = 0

    private lateinit var viewModel: TransactionViewModel
    private var payloadLoaderTask: PayloadLoaderTask? = null
    private var fileSaverTask: FileSaverTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt(ARG_TYPE)
        viewModel = ViewModelProviders.of(requireActivity())[TransactionViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.chucker_fragment_transaction_payload, container, false).apply {
            transactionContentList = findViewById(R.id.transaction_content)
            transactionContentList.isNestedScrollingEnabled = false
            progressLoading = findViewById(R.id.progress_loading_transaction)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transaction.observe(
            this,
            Observer { transaction ->
                PayloadLoaderTask(this).execute(Pair(type, transaction))
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        payloadLoaderTask?.cancel(true)
        fileSaverTask?.cancel(true)
    }

    @SuppressLint("NewApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val transaction = viewModel.transaction.value

        val showSearchIcon = when {
            (type == TYPE_REQUEST && true == transaction?.isRequestBodyPlainText) -> true
            (type == TYPE_RESPONSE && true == transaction?.isResponseBodyPlainText) -> true
            else -> false
        }

        if (showSearchIcon) {
            val searchMenuItem = menu.findItem(R.id.search)
            searchMenuItem.isVisible = true
            val searchView = searchMenuItem.actionView as SearchView
            searchView.setOnQueryTextListener(this)
            searchView.setIconifiedByDefault(true)
        }

        val showSaveMenuItem = when {
            // SAF is not available on pre-Kit Kat so let's hide the icon.
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) -> false
            (type == TYPE_REQUEST && 0L == (transaction?.requestContentLength ?: 0L)) -> false
            (type == TYPE_RESPONSE && 0L == (transaction?.responseContentLength ?: 0L)) -> false
            else -> true
        }

        if (showSaveMenuItem) {
            menu.findItem(R.id.save_body).apply {
                isVisible = true
                setOnMenuItemClickListener {
                    viewBodyExternally()
                    true
                }
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backgroundSpanColor = ContextCompat.getColor(context, R.color.chucker_background_span_color)
        foregroundSpanColor = ContextCompat.getColor(context, R.color.chucker_foreground_span_color)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("DefaultLocale")
    private fun viewBodyExternally() {
        viewModel.transaction.value?.let {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, "$DEFAULT_FILE_PREFIX${System.currentTimeMillis()}")
                type = "*/*"
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, GET_FILE_FOR_SAVING_REQUEST_CODE)
            } else {
                Toast.makeText(
                    requireContext(), R.string.chucker_save_failed_to_open_document,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == GET_FILE_FOR_SAVING_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = resultData?.data
            val transaction = viewModel.transaction.value
            if (uri != null && transaction != null) {
                fileSaverTask = FileSaverTask(this).apply {
                    execute(Triple(type, uri, transaction))
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        val adapter = (transactionContentList.adapter as TransactionBodyAdapter)
        if (newText.isNotBlank()) {
            adapter.highlightQueryWithColors(newText, backgroundSpanColor, foregroundSpanColor)
        } else {
            adapter.resetHighlight()
        }
        return true
    }

    /**
     * Async task responsible of loading in the background the content of the HTTP request/response.
     */
    class PayloadLoaderTask(private val fragment: TransactionPayloadFragment) :
        AsyncTask<Pair<Int, HttpTransaction>, Unit, List<TransactionPayloadItem>>() {

        override fun onPreExecute() {
            val progressBar: ProgressBar? = fragment.view?.findViewById(R.id.progress_loading_transaction)
            val recyclerView: RecyclerView? = fragment.view?.findViewById(R.id.transaction_content)
            progressBar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.INVISIBLE
        }

        @Suppress("ComplexMethod")
        override fun doInBackground(vararg params: Pair<Int, HttpTransaction>): List<TransactionPayloadItem> {
            val (type, transaction) = params[0]
            val result = mutableListOf<TransactionPayloadItem>()

            val headersString: String
            val isBodyPlainText: Boolean
            val bodyString: String

            if (type == TYPE_REQUEST) {
                headersString = transaction.getRequestHeadersString(true)
                isBodyPlainText = transaction.isRequestBodyPlainText
                bodyString = transaction.getFormattedRequestBody()
            } else {
                headersString = transaction.getResponseHeadersString(true)
                isBodyPlainText = transaction.isResponseBodyPlainText
                bodyString = transaction.getFormattedResponseBody()
            }

            if (headersString.isNotBlank()) {
                result.add(
                    TransactionPayloadItem.HeaderItem(
                        HtmlCompat.fromHtml(headersString, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    )
                )
            }

            // The body could either be an image, binary encoded or plain text.
            val responseBitmap = transaction.responseImageBitmap
            if (type == TYPE_RESPONSE && responseBitmap != null) {
                result.add(TransactionPayloadItem.ImageItem(responseBitmap))
            } else if (!isBodyPlainText) {
                fragment.context?.getString(R.string.chucker_body_omitted)?.let {
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                }
            } else {
                bodyString.lines().forEach {
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                }
            }

            return result
        }

        override fun onPostExecute(result: List<TransactionPayloadItem>) {
            val progressBar: ProgressBar? = fragment.view?.findViewById(R.id.progress_loading_transaction)
            val recyclerView: RecyclerView? = fragment.view?.findViewById(R.id.transaction_content)
            progressBar?.visibility = View.INVISIBLE
            recyclerView?.visibility = View.VISIBLE
            recyclerView?.adapter = TransactionBodyAdapter(result)
        }
    }

    class FileSaverTask(val fragment: TransactionPayloadFragment) :
        AsyncTask<Triple<Int, Uri, HttpTransaction>, Unit, Boolean>() {

        @Suppress("NestedBlockDepth")
        override fun doInBackground(vararg params: Triple<Int, Uri, HttpTransaction>): Boolean {
            val (type, uri, transaction) = params[0]
            try {
                val context = fragment.context ?: return false
                context.contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fos ->
                        when {
                            type == TYPE_REQUEST -> {
                                transaction.requestBody?.byteInputStream()?.copyTo(fos)
                            }
                            transaction.responseBody != null -> {
                                transaction.responseBody?.byteInputStream()?.copyTo(fos)
                            }
                            else -> {
                                fos.write(transaction.responseImageData)
                            }
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }

        override fun onPostExecute(isSuccessful: Boolean) {
            fragment.fileSaverTask = null
            val toastMessageId = if (isSuccessful) {
                R.string.chucker_file_saved
            } else {
                R.string.chucker_file_not_saved
            }
            Toast.makeText(fragment.context, toastMessageId, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_TYPE = "type"

        const val TYPE_REQUEST = 0
        const val TYPE_RESPONSE = 1

        const val DEFAULT_FILE_PREFIX = "chucker-export-"

        fun newInstance(type: Int): TransactionPayloadFragment =
            TransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, type)
                }
            }
    }
}
