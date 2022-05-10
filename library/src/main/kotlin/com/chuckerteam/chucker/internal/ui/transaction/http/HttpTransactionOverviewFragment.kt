package com.chuckerteam.chucker.internal.ui.transaction.http

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionOverviewBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class HttpTransactionOverviewFragment : Fragment() {
    private val sharedViewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }
    private val viewModel: HttpTransactionViewModel by viewModels { HttpTransactionViewModelFactory(sharedViewModel) }

    private lateinit var overviewBinding: ChuckerFragmentTransactionOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        overviewBinding = ChuckerFragmentTransactionOverviewBinding.inflate(inflater, container, false)
        return overviewBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.save_body).isVisible = false
        lifecycleScope.launch {
            viewModel.doesUrlRequireEncoding.collect {
                menu.findItem(R.id.encode_url).isVisible = it
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.transaction
                .combine(viewModel.encodeUrl) { transaction, encodeUrl ->
                    Pair(transaction, encodeUrl)
                }.collect { pair ->
                    populateUI(pair.first, pair.second)
                }
        }
    }

    private fun populateUI(transaction: HttpTransaction?, encodeUrl: Boolean) {
        with(overviewBinding) {
            url.text = transaction?.getFormattedUrl(encodeUrl)
            method.text = transaction?.method
            protocol.text = transaction?.protocol
            status.text = transaction?.status.toString()
            response.text = transaction?.responseSummaryText
            when (transaction?.isSsl) {
                null -> {
                    sslGroup.visibility = View.GONE
                }
                true -> {
                    sslGroup.visibility = View.VISIBLE
                    sslValue.setText(R.string.chucker_yes)
                }
                else -> {
                    sslGroup.visibility = View.VISIBLE
                    sslValue.setText(R.string.chucker_no)
                }
            }
            if (transaction?.responseTlsVersion != null) {
                tlsVersionValue.text = transaction.responseTlsVersion
                tlsGroup.visibility = View.VISIBLE
            }
            if (transaction?.responseCipherSuite != null) {
                cipherSuiteValue.text = transaction.responseCipherSuite
                cipherSuiteGroup.visibility = View.VISIBLE
            }
            requestTime.text = transaction?.requestDateString
            responseTime.text = transaction?.responseDateString
            duration.text = transaction?.durationString
            requestSize.text = transaction?.requestSizeString
            responseSize.text = transaction?.responseSizeString
            totalSize.text = transaction?.totalSizeString
        }
    }
}
