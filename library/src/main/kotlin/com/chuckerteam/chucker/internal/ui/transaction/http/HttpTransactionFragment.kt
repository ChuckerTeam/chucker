package com.chuckerteam.chucker.internal.ui.transaction.http

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionHttpBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.Sharable
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class HttpTransactionFragment : Fragment() {
    private val sharedViewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }
    private val viewModel : HttpTransactionViewModel by viewModels { HttpTransactionViewModelFactory(sharedViewModel) }

    private lateinit var viewBinding: ChuckerFragmentTransactionHttpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = ChuckerFragmentTransactionHttpBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            setupViewPager(viewPager)
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            tabLayout.setupWithViewPager(viewPager)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            viewModel.transactionTitle.collect {
                viewBinding.toolbarTitle.text = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setUpUrlEncoding(menu)
    }

    private fun setUpUrlEncoding(menu: Menu) {
        val encodeUrlMenuItem = menu.findItem(R.id.encode_url)
        encodeUrlMenuItem.setOnMenuItemClickListener {
            viewModel.switchUrlEncoding()
            return@setOnMenuItemClickListener true
        }
        lifecycleScope.launch {
            viewModel.encodeUrl.collect {encode ->
                val icon = if (encode) {
                    R.drawable.chucker_ic_encoded_url_white
                } else {
                    R.drawable.chucker_ic_decoded_url_white
                }
                encodeUrlMenuItem.setIcon(icon)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share_text -> shareTransactionAsText { transaction ->
            val encodeUrls = viewModel.encodeUrl.value
            HttpTransactionDetailsSharable(transaction, encodeUrls)
        }
        R.id.share_curl -> shareTransactionAsText { transaction ->
            TransactionCurlCommandSharable(transaction)
        }
        R.id.share_file -> shareTransactionAsFile(EXPORT_TXT_FILE_NAME) { transaction ->
            val encodeUrls = viewModel.encodeUrl.value
            HttpTransactionDetailsSharable(transaction, encodeUrls)
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

    private fun shareTransactionAsText(block: (HttpTransaction) -> Sharable): Boolean {
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

    private fun shareTransactionAsFile(fileName: String, block: suspend (HttpTransaction) -> Sharable): Boolean {
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

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = HttpTransactionPagerAdapter(requireContext(), childFragmentManager)
        viewPager.addOnPageChangeListener(
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    selectedTabPosition = position
                }
            }
        )
        viewPager.currentItem = selectedTabPosition
    }

    companion object {
        private var selectedTabPosition = 0
    }

}
