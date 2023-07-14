@file:Suppress("TooManyFunctions")

package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionPayloadBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.calculateLuminance
import com.chuckerteam.chucker.internal.support.combineLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

internal class TransactionPayloadFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }

    private val payloadType: PayloadType by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getSerializable(ARG_TYPE) as PayloadType
    }

    private val saveToFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
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
    private val payloadAdapter = TransactionBodyAdapter()

    private var backgroundSpanColor: Int = Color.YELLOW
    private var foregroundSpanColor: Int = Color.RED
    private var backgroundSpanColorSearchItem: Int = Color.GREEN

    private val scrollableIndices = arrayListOf<TransactionBodyAdapter.SearchItemBodyLine>()
    private var currentSearchScrollIndex = -1
    private var currentSearchQuery: String = ""

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

        viewModel.transaction.combineLatest(viewModel.formatRequestBody).observe(
            viewLifecycleOwner,
            Observer { (transaction, formatRequestBody) ->
                if (transaction == null) return@Observer
                lifecycleScope.launch {
                    payloadBinding.loadingProgress.visibility = View.VISIBLE

                    val result = processPayload(payloadType, transaction, formatRequestBody)
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
        )
        payloadBinding.searchNavButton.setOnClickListener {
            onSearchScrollerButtonClick(true)
        }
        payloadBinding.searchNavButtonUp.setOnClickListener {
            onSearchScrollerButtonClick(false)
        }
    }

    private fun onSearchScrollerButtonClick(goNext: Boolean) {
        // hide the keyboard if visible
        val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            activity?.currentFocus?.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }

        if (scrollableIndices.isNotEmpty()) {
            val scrollToIndex =
                if (goNext) {
                    ((currentSearchScrollIndex + 1) % scrollableIndices.size)
                } else {
                    (abs(currentSearchScrollIndex - 1 + scrollableIndices.size) % scrollableIndices.size)
                }

            scrollToSearchedItemPosition(scrollToIndex)
        }
    }

    private fun showEmptyState() {
        payloadBinding.apply {
            emptyPayloadTextView.text = if (payloadType == PayloadType.RESPONSE) {
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

        if (payloadType == PayloadType.REQUEST) {
            viewModel.doesRequestBodyRequireEncoding.observe(
                viewLifecycleOwner,
                { menu.findItem(R.id.encode_url).isVisible = it }
            )
        } else {
            menu.findItem(R.id.encode_url).isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun shouldShowSaveIcon(transaction: HttpTransaction?) = when {
        (payloadType == PayloadType.REQUEST) -> (0L != (transaction?.requestPayloadSize))
        (payloadType == PayloadType.RESPONSE) -> (0L != (transaction?.responsePayloadSize))
        else -> true
    }

    private fun shouldShowSearchIcon(transaction: HttpTransaction?) = when (payloadType) {
        PayloadType.REQUEST -> {
            (false == transaction?.isRequestBodyEncoded) && (0L != (transaction.requestPayloadSize))
        }
        PayloadType.RESPONSE -> {
            (false == transaction?.isResponseBodyEncoded) && (0L != (transaction.responsePayloadSize))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backgroundSpanColor = ContextCompat.getColor(context, R.color.chucker_background_span_color)
        foregroundSpanColor = ContextCompat.getColor(context, R.color.chucker_foreground_span_color)
    }

    private fun createFileToSaveBody() {
        val transaction = viewModel.transaction.value
        if (transaction != null && isBodyEmpty(payloadType, transaction)) {
            Toast.makeText(
                activity,
                R.string.chucker_file_not_saved_body_is_empty,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            saveToFile.launch("$DEFAULT_FILE_PREFIX${System.currentTimeMillis()}")
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        scrollableIndices.clear()
        currentSearchQuery = newText
        currentSearchScrollIndex = -1

        if (newText.isNotBlank() && newText.length > NUMBER_OF_IGNORED_SYMBOLS) {
            val listOfSearchQuery = payloadAdapter.highlightQueryWithColors(
                newText,
                backgroundSpanColor,
                foregroundSpanColor
            )
            if (listOfSearchQuery.isNotEmpty()) {
                scrollableIndices.addAll(listOfSearchQuery)
            } else {
                payloadAdapter.resetHighlight()
                makeToolbarSearchSummaryVisible(false)
            }
        } else {
            payloadAdapter.resetHighlight()
            makeToolbarSearchSummaryVisible(false)
        }

        lifecycleScope.launch {
            delay(DELAY_FOR_SEARCH_SCROLL)
            lifecycle.withResumed {
                if (scrollableIndices.isNotEmpty()) {
                    scrollToSearchedItemPosition(0)
                } else {
                    currentSearchScrollIndex = -1
                }
            }
        }
        return true
    }

    private fun makeToolbarSearchSummaryVisible(visible: Boolean = true) {
        payloadBinding.rootSearchSummary.isVisible = visible
    }

    private fun updateToolbarText(searchResultsCount: Int, currentIndex: Int = 1) {
        payloadBinding.searchSummary.text = SpannableStringBuilder().apply {
            bold {
                append("$currentIndex / $searchResultsCount")
            }
        }
    }

    private fun scrollToSearchedItemPosition(positionOfScrollableIndices: Int) {
        // reset the last searched item highlight if done
        scrollableIndices.getOrNull(currentSearchScrollIndex)?.let {
            payloadAdapter.highlightItemWithColorOnPosition(
                it.indexBodyLine,
                it.indexStartOfQuerySubString,
                currentSearchQuery,
                backgroundSpanColor,
                foregroundSpanColor
            )
        }

        currentSearchScrollIndex = positionOfScrollableIndices
        val scrollTo = scrollableIndices.getOrNull(positionOfScrollableIndices)
        if (scrollTo != null) {
            // highlight the next navigated item and update toolbar summary text
            payloadAdapter.highlightItemWithColorOnPosition(
                scrollTo.indexBodyLine,
                scrollTo.indexStartOfQuerySubString,
                currentSearchQuery,
                backgroundSpanColorSearchItem,
                foregroundSpanColor
            )
            updateToolbarText(scrollableIndices.size, positionOfScrollableIndices + 1)
            makeToolbarSearchSummaryVisible()

            payloadBinding.payloadRecyclerView.smoothScrollToPosition(scrollTo.indexBodyLine)
            currentSearchScrollIndex = positionOfScrollableIndices
        }
    }

    private suspend fun processPayload(
        type: PayloadType,
        transaction: HttpTransaction,
        formatRequestBody: Boolean
    ): MutableList<TransactionPayloadItem> {
        return withContext(Dispatchers.Default) {
            val result = mutableListOf<TransactionPayloadItem>()

            val headersString: String
            val isBodyEncoded: Boolean
            val bodyString: CharSequence

            if (type == PayloadType.REQUEST) {
                headersString = transaction.getRequestHeadersString(true)
                isBodyEncoded = transaction.isRequestBodyEncoded
                bodyString = if (formatRequestBody) {
                    transaction.getSpannedRequestBody(context)
                } else {
                    transaction.requestBody ?: ""
                }
            } else {
                headersString = transaction.getResponseHeadersString(true)
                isBodyEncoded = transaction.isResponseBodyEncoded
                bodyString = transaction.getSpannedResponseBody(context)
            }
            if (headersString.isNotBlank()) {
                result.add(
                    TransactionPayloadItem.HeaderItem(
                        HtmlCompat.fromHtml(
                            headersString,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                )
            }

            // The body could either be an image, plain text, decoded binary or not decoded binary.
            val responseBitmap = transaction.responseImageBitmap

            if (type == PayloadType.RESPONSE && responseBitmap != null) {
                val bitmapLuminance = responseBitmap.calculateLuminance()
                result.add(TransactionPayloadItem.ImageItem(responseBitmap, bitmapLuminance))
                return@withContext result
            }

            when {
                isBodyEncoded -> {
                    val text = requireContext().getString(R.string.chucker_body_omitted)
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
                }

                bodyString.isBlank() -> {
                    val text = requireContext().getString(R.string.chucker_body_empty)
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
                }

                else -> bodyString.lines().forEach {
                    result.add(
                        TransactionPayloadItem.BodyLineItem(
                            if (it is SpannableStringBuilder) {
                                it
                            } else {
                                SpannableStringBuilder.valueOf(it)
                            }
                        )
                    )
                }
            }
            return@withContext result
        }
    }

    private suspend fun saveToFile(type: PayloadType, uri: Uri, transaction: HttpTransaction): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fos ->
                        when (type) {
                            PayloadType.REQUEST -> {
                                transaction.requestBody?.byteInputStream()?.copyTo(fos)
                                    ?: throw IOException(TRANSACTION_EXCEPTION)
                            }

                            PayloadType.RESPONSE -> {
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

    private fun isBodyEmpty(type: PayloadType, transaction: HttpTransaction): Boolean =
        when {
            type == PayloadType.REQUEST && transaction.requestBody == null -> true
            type == PayloadType.RESPONSE && transaction.responseBody == null -> true
            else -> false
        }

    companion object {
        private const val ARG_TYPE = "type"
        private const val TRANSACTION_EXCEPTION = "Transaction not ready"
        private const val DELAY_FOR_SEARCH_SCROLL: Long = 600L

        private const val NUMBER_OF_IGNORED_SYMBOLS = 1

        const val DEFAULT_FILE_PREFIX = "chucker-export-"

        fun newInstance(type: PayloadType): TransactionPayloadFragment =
            TransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
    }

    private fun CharSequence.lines(): List<CharSequence> {
        val linesList = this.lineSequence().toList()
        val result = mutableListOf<CharSequence>()
        var lineIndex = 0
        for (index in linesList.indices) {
            result.add(subSequence(lineIndex, lineIndex + linesList[index].length))
            lineIndex += linesList[index].length + 1
        }
        if (result.isEmpty()) {
            result.add(subSequence(0, length))
        }
        return result
    }
}
