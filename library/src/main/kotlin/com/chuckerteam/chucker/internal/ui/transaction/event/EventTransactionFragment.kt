package com.chuckerteam.chucker.internal.ui.transaction.event

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionEventBinding
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.share.EventTransactionDetailsSharable
import com.chuckerteam.chucker.internal.support.share.HttpTransactionDetailsSharable
import com.chuckerteam.chucker.internal.support.share.TransactionCurlCommandSharable
import com.chuckerteam.chucker.internal.support.share.TransactionDetailsHarSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.shareAsUtf8Text
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity.Companion.EXPORT_HAR_FILE_NAME
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity.Companion.EXPORT_TXT_FILE_NAME
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModelFactory
import com.chuckerteam.chucker.internal.ui.transaction.http.HttpTransactionFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class EventTransactionFragment : Fragment() {
    private val sharedViewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }
    private val viewModel: EventTransactionViewModel by viewModels {
        EventTransactionViewModelFactory(
            sharedViewModel
        )
    }
    private lateinit var viewBinding: ChuckerFragmentTransactionEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = ChuckerFragmentTransactionEventBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            viewModel.transaction.collect {
                viewBinding.toolbarTitle.text = it?.title ?: "No Title"
                viewBinding.payloadText.text = it?.payload ?: "No Payload"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.share_curl).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share_text -> shareTransactionAsText { transaction ->
            EventTransactionDetailsSharable(transaction)
        }
        R.id.share_file -> shareTransactionAsFile(EXPORT_TXT_FILE_NAME) { transaction ->
            EventTransactionDetailsSharable(transaction)
        }
        R.id.share_har -> shareTransactionAsFile(EXPORT_HAR_FILE_NAME) { transaction ->
            TransactionDetailsHarSharable(
                HarUtils.harStringFromTransactions(
                    listOf(transaction),
                    getString(R.string.chucker_name),
                    getString(R.string.chucker_version)
                )
            )
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun shareTransactionAsText(block: (EventTransaction) -> Sharable): Boolean {
        val transaction = viewModel.transaction.value
        if (transaction == null) {
            (activity as BaseChuckerActivity).showToast(getString(R.string.chucker_request_not_ready))
            return true
        }
        val sharable = block(transaction)
        lifecycleScope.launch {
            val shareIntent = sharable.shareAsUtf8Text(
                activity = requireActivity(),
                intentTitle = getString(R.string.chucker_share_transaction_title),
                intentSubject = getString(R.string.chucker_share_transaction_subject)
            )
            startActivity(shareIntent)
        }
        return true
    }

    private fun shareTransactionAsFile(fileName: String, block: suspend (EventTransaction) -> Sharable): Boolean {
        lifecycleScope.launch {
            val transaction = viewModel.transaction.value
            if (transaction == null) {
                (activity as BaseChuckerActivity).showToast(getString(R.string.chucker_request_not_ready))
                return@launch
            }

            val sharable = block(transaction)
            lifecycleScope.launch {
                val shareIntent = sharable.shareAsFile(
                    activity = requireActivity(),
                    fileName = fileName,
                    intentTitle = getString(R.string.chucker_share_transaction_title),
                    intentSubject = getString(R.string.chucker_share_transaction_subject),
                    clipDataLabel = "transaction"
                )
                if (shareIntent != null) {
                    startActivity(shareIntent)
                }
            }
        }
        return true
    }


}
