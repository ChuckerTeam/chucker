package com.chuckerteam.chucker.internal.ui.transaction.http

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionPayloadBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.calculateLuminance
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

internal class HttpTransactionPayloadFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private val sharedViewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }
    private val viewModel: HttpTransactionViewModel by viewModels { HttpTransactionViewModelFactory(sharedViewModel) }

    private val payloadType: HttpPayloadType by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getSerializable(ARG_TYPE) as HttpPayloadType
    }

    private val saveToFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        val transaction = viewModel.transaction.value
        val applicationContext = requireContext().applicationContext
        if (uri != null && transaction != null) {
            lifecycleScope.launch {
                val result = saveToFile(payloadType, uri, transaction)
                val toastMessageId = if (result) {
                    R.string.chucker_file_saved
                } else {
                    R.string.chucker_file_not_saved
                }
                Toast.makeText(applicationContext, toastMessageId, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                R.string.chucker_save_failed_to_open_document,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var payloadBinding: ChuckerFragmentTransactionPayloadBinding
    private val payloadAdapter = HttpTransactionBodyAdapter()

    private var backgroundSpanColor: Int = Color.YELLOW
    private var foregroundSpanColor: Int = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        payloadBinding = ChuckerFragmentTransactionPayloadBinding.inflate(
            inflater,
            container,
            false
        )
        return payloadBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        payloadBinding.payloadRecyclerView.apply {
            setHasFixedSize(true)
            adapter = payloadAdapter
        }

        lifecycleScope.launch {
            viewModel.transaction
                .combine(viewModel.formatRequestBody) { first, second ->
                    Pair(first, second)
                }.collect {
                    if (it.first == null) return@collect
                    payloadBinding.loadingProgress.visibility = View.VISIBLE

                    val result = processPayload(payloadType, it.first!!, it.second)
                    if (result.isEmpty()) {
                        showEmptyState()
                    } else {
                        payloadAdapter.setItems(result)
                        showPayloadState()
                    }
                    // Invalidating menu, because we need to hide menu items for empty payloads
                    requireActivity().invalidateOptionsMenu()

                    payloadBinding.loadingProgress.visibility = View.GONE
                }
        }

        lifecycleScope.launch {
            viewModel.transaction
                .combine(viewModel.formatRequestBody) { first, second ->
                    Pair(first, second)
                }.collect { (_, _) ->

                }
        }
    }

    private fun showEmptyState() {
        payloadBinding.apply {
            emptyPayloadTextView.text = if (payloadType == HttpPayloadType.RESPONSE) {
                getString(R.string.chucker_response_is_empty)
            } else {
                getString(R.string.chucker_request_is_empty)
            }
            emptyStateGroup.visibility = View.VISIBLE
            payloadRecyclerView.visibility = View.GONE
        }
    }

    private fun showPayloadState() {
        payloadBinding.apply {
            emptyStateGroup.visibility = View.GONE
            payloadRecyclerView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NewApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val transaction = viewModel.transaction.value

        if (shouldShowSearchIcon(transaction)) {
            val searchMenuItem = menu.findItem(R.id.search)
            searchMenuItem.isVisible = true
            val searchView = searchMenuItem.actionView as SearchView
            searchView.setOnQueryTextListener(this)
            searchView.setIconifiedByDefault(true)
        }

        if (shouldShowSaveIcon(transaction)) {
            menu.findItem(R.id.save_body).apply {
                isVisible = true
                setOnMenuItemClickListener {
                    createFileToSaveBody()
                    true
                }
            }
        }

        if (payloadType == HttpPayloadType.REQUEST) {
            lifecycleScope.launch {
                viewModel.doesRequestBodyRequireEncoding.collect {
                    menu.findItem(R.id.encode_url).isVisible = it
                }
            }

        } else {
            menu.findItem(R.id.encode_url).isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun shouldShowSaveIcon(transaction: HttpTransaction?) = when {
        (payloadType == HttpPayloadType.REQUEST) -> (0L != (transaction?.requestPayloadSize))
        (payloadType == HttpPayloadType.RESPONSE) -> (0L != (transaction?.responsePayloadSize))
        else -> true
    }

    private fun shouldShowSearchIcon(transaction: HttpTransaction?) = when (payloadType) {
        HttpPayloadType.REQUEST -> {
            (false == transaction?.isRequestBodyEncoded) && (0L != (transaction.requestPayloadSize))
        }
        HttpPayloadType.RESPONSE -> {
            (false == transaction?.isResponseBodyEncoded) && (0L != (transaction.responsePayloadSize))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backgroundSpanColor = ContextCompat.getColor(context, R.color.chucker_background_span_color)
        foregroundSpanColor = ContextCompat.getColor(context, R.color.chucker_foreground_span_color)
    }

    private fun createFileToSaveBody() {
        saveToFile.launch("$DEFAULT_FILE_PREFIX${System.currentTimeMillis()}")
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotBlank() && newText.length > NUMBER_OF_IGNORED_SYMBOLS) {
            payloadAdapter.highlightQueryWithColors(newText, backgroundSpanColor, foregroundSpanColor)
        } else {
            payloadAdapter.resetHighlight()
        }
        return true
    }

    private suspend fun processPayload(
        type: HttpPayloadType,
        transaction: HttpTransaction,
        formatRequestBody: Boolean
    ): MutableList<HttpTransactionPayloadItem> {
        return withContext(Dispatchers.Default) {
            val result = mutableListOf<HttpTransactionPayloadItem>()

            val headersString: String
            val isBodyEncoded: Boolean
            val bodyString: String

            if (type == HttpPayloadType.REQUEST) {
                headersString = transaction.getRequestHeadersString(true)
                isBodyEncoded = transaction.isRequestBodyEncoded
                bodyString = if (formatRequestBody) {
                    transaction.getFormattedRequestBody()
                } else {
                    transaction.requestBody ?: ""
                }
            } else {
                headersString = transaction.getResponseHeadersString(true)
                isBodyEncoded = transaction.isResponseBodyEncoded
                bodyString = transaction.getFormattedResponseBody()
            }

            if (headersString.isNotBlank()) {
                result.add(
                    HttpTransactionPayloadItem.HeaderItem(
                        HtmlCompat.fromHtml(
                            headersString,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                )
            }

            // The body could either be an image, plain text, decoded binary or not decoded binary.
            val responseBitmap = transaction.responseImageBitmap

            if (type == HttpPayloadType.RESPONSE && responseBitmap != null) {
                val bitmapLuminance = responseBitmap.calculateLuminance()
                result.add(HttpTransactionPayloadItem.ImageItem(responseBitmap, bitmapLuminance))
                return@withContext result
            }

            when {
                isBodyEncoded -> {
                    val text = requireContext().getString(R.string.chucker_body_omitted)
                    result.add(
                        HttpTransactionPayloadItem.BodyLineItem(
                            SpannableStringBuilder.valueOf(
                                text
                            )
                        )
                    )
                }
                bodyString.isBlank() -> {
                    val text = requireContext().getString(R.string.chucker_body_empty)
                    result.add(
                        HttpTransactionPayloadItem.BodyLineItem(
                            SpannableStringBuilder.valueOf(
                                text
                            )
                        )
                    )
                }
                else -> bodyString.lines().forEach {
                    result.add(HttpTransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                }
            }

            return@withContext result
        }
    }

    private suspend fun saveToFile(type: HttpPayloadType, uri: Uri, transaction: HttpTransaction): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fos ->
                        when (type) {
                            HttpPayloadType.REQUEST -> {
                                transaction.requestBody?.byteInputStream()?.copyTo(fos)
                                    ?: throw IOException(TRANSACTION_EXCEPTION)
                            }
                            HttpPayloadType.RESPONSE -> {
                                transaction.responseBody?.byteInputStream()?.copyTo(fos)
                                    ?: throw IOException(TRANSACTION_EXCEPTION)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Logger.error("Failed to save transaction to a file", e)
                return@withContext false
            }
            return@withContext true
        }
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val TRANSACTION_EXCEPTION = "Transaction not ready"

        private const val NUMBER_OF_IGNORED_SYMBOLS = 1

        const val DEFAULT_FILE_PREFIX = "chucker-export-"

        fun newInstance(type: HttpPayloadType): HttpTransactionPayloadFragment =
            HttpTransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
    }
}
