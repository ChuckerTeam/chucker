package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionOverviewBinding
import com.chuckerteam.chucker.internal.support.combineLatest

internal class TransactionOverviewFragment : Fragment() {

    private lateinit var overviewBinding: ChuckerFragmentTransactionOverviewBinding
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
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

        viewModel.transaction
            .combineLatest(viewModel.encodeUrl)
            .observe(
                viewLifecycleOwner,
                Observer { (transaction, encodeUrl) ->
                    with(overviewBinding) {
                        chuckerTransactionOverviewUrl.text = transaction?.getFormattedUrl(encodeUrl)
                        chuckerTransactionOverviewMethod.text = transaction?.method
                        chuckerTransactionOverviewProtocol.text = transaction?.protocol
                        chuckerTransactionOverviewStatus.text = transaction?.status.toString()
                        chuckerTransactionOverviewResponse.text = transaction?.responseSummaryText
                        chuckerTransactionOverviewSsl.setText(
                            if (transaction?.isSsl == true) R.string.chucker_yes else R.string.chucker_no
                        )
                        chuckerTransactionOverviewRequestTime.text = transaction?.requestDateString
                        chuckerTransactionOverviewResponseTime.text = transaction?.responseDateString
                        chuckerTransactionOverviewDuration.text = transaction?.durationString
                        chuckerTransactionOverviewRequestSize.text = transaction?.requestSizeString
                        chuckerTransactionOverviewResponseSize.text = transaction?.responseSizeString
                        chuckerTransactionOverviewTotalSize.text = transaction?.totalSizeString
                    }
                }
            )
    }
}
