package com.chuckerteam.chucker.internal.ui.transaction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionPayloadBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.calculateLuminance
import com.chuckerteam.chucker.internal.support.combineLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val GET_FILE_FOR_SAVING_REQUEST_CODE: Int = 43

internal class TransactionPayloadFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }

    private val payloadType: PayloadType by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getSerializable(ARG_TYPE) as PayloadType
    }

    private lateinit var payloadBinding: ChuckerFragmentTransactionPayloadBinding
    private val payloadAdapter = TransactionBodyAdapter()

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
    ): View? {
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

        viewModel.transaction
            .combineLatest(viewModel.formatRequestBody)
            .observe(
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
                Observer { menu.findItem(R.id.encode_url).isVisible = it }
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
            (true == transaction?.isRequestBodyPlainText) && (0L != (transaction.requestPayloadSize))
        }
        PayloadType.RESPONSE -> {
            (true == transaction?.isResponseBodyPlainText) && (0L != (transaction.responsePayloadSize))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backgroundSpanColor = ContextCompat.getColor(context, R.color.chucker_background_span_color)
        foregroundSpanColor = ContextCompat.getColor(context, R.color.chucker_foreground_span_color)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createFileToSaveBody() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, "$DEFAULT_FILE_PREFIX${System.currentTimeMillis()}")
            type = "*/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, GET_FILE_FOR_SAVING_REQUEST_CODE)
        } else {
            Toast.makeText(
                requireContext(),
                R.string.chucker_save_failed_to_open_document,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == GET_FILE_FOR_SAVING_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = resultData?.data
            val transaction = viewModel.transaction.value
            if (uri != null && transaction != null) {
                lifecycleScope.launch {
                    val result = saveToFile(payloadType, uri, transaction)
                    val toastMessageId = if (result) {
                        R.string.chucker_file_saved
                    } else {
                        R.string.chucker_file_not_saved
                    }
                    Toast.makeText(context, toastMessageId, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, R.string.chucker_file_not_saved, Toast.LENGTH_SHORT).show()
            }
        }
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
        type: PayloadType,
        transaction: HttpTransaction,
        formatRequestBody: Boolean
    ): MutableList<TransactionPayloadItem> {
        return withContext(Dispatchers.Default) {
            val result = mutableListOf<TransactionPayloadItem>()

            val headersString: String
            val isBodyPlainText: Boolean
            val bodyString: String

            if (type == PayloadType.REQUEST) {
                headersString = transaction.getRequestHeadersString(true)
                isBodyPlainText = transaction.isRequestBodyPlainText
                bodyString = if (formatRequestBody) {
                    transaction.getFormattedRequestBody()
                } else {
                    transaction.requestBody ?: ""
                }
            } else {
                headersString = transaction.getResponseHeadersString(true)
                isBodyPlainText = transaction.isResponseBodyPlainText
                bodyString = transaction.getFormattedResponseBody()
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

            // The body could either be an image, binary encoded or plain text.
            val responseBitmap = transaction.responseImageBitmap
            if (type == PayloadType.RESPONSE && responseBitmap != null) {
                val bitmapLuminance = responseBitmap.calculateLuminance()
                result.add(TransactionPayloadItem.ImageItem(responseBitmap, bitmapLuminance))
            } else if (!isBodyPlainText) {
                requireContext().getString(R.string.chucker_body_omitted).let {
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                }
            } else {
                if (bodyString.isNotBlank()) {
                    bodyString.lines().forEach {
                        result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                    }
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
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return@withContext false
            } catch (e: IOException) {
                e.printStackTrace()
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

        fun newInstance(type: PayloadType): TransactionPayloadFragment =
            TransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
    }
}
