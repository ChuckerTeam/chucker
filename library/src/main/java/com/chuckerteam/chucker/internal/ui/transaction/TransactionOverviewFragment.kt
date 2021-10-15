package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionOverviewBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.combineLatest

internal class TransactionOverviewFragment : Fragment() {

    private val viewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }

    private lateinit var overviewBinding: ChuckerFragmentTransactionOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        overviewBinding = ChuckerFragmentTransactionOverviewBinding.inflate(inflater, container, false)
        return overviewBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.save_body).isVisible = false
        viewModel.doesUrlRequireEncoding.observe(
            viewLifecycleOwner,
            Observer { menu.findItem(R.id.encode_url).isVisible = it }
        )

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transaction.combineLatest(viewModel.encodeUrl).observe(
            viewLifecycleOwner,
            Observer { (transaction, encodeUrl) -> populateUI(transaction, encodeUrl) }
        )
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
