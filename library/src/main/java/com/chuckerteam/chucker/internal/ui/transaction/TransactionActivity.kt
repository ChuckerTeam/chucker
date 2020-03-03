package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityTransactionBinding
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareCurlCommand
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareText
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

internal class TransactionActivity : BaseChuckerActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionBinding: ChuckerActivityTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionBinding = ChuckerActivityTransactionBinding.inflate(layoutInflater)

        val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0)

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        viewModel = ViewModelProvider(this, TransactionViewModelFactory(transactionId))
            .get(TransactionViewModel::class.java)

        with(transactionBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            setupViewPager(viewPager)
            tabLayout.setupWithViewPager(viewPager)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.transactionTitle.observe(
            this,
            Observer { transactionBinding.toolbarTitle.text = it }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chucker_transaction, menu)
        setUpUrlEncoding(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpUrlEncoding(menu: Menu) {
        val encodeUrlMenuItem = menu.findItem(R.id.encode_url)
        encodeUrlMenuItem.setOnMenuItemClickListener {
            viewModel.switchUrlEncoding()
            return@setOnMenuItemClickListener true
        }
        viewModel.encodeUrl.observe(
            this,
            Observer { encode ->
                val icon = if (encode) {
                    R.drawable.chucker_ic_encoded_url_white
                } else {
                    R.drawable.chucker_ic_decoded_url_white
                }
                encodeUrlMenuItem.setIcon(icon)
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.share_text -> {
                viewModel.transaction.value?.let {
                    share(getShareText(this, it, viewModel.encodeUrl.value!!))
                } ?: showToast(getString(R.string.chucker_request_not_ready))
                true
            }
            R.id.share_curl -> {
                viewModel.transaction.value?.let {
                    share(getShareCurlCommand(it))
                } ?: showToast(getString(R.string.chucker_request_not_ready))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = TransactionPagerAdapter(this, supportFragmentManager)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                selectedTabPosition = position
            }
        })
        viewPager.currentItem = selectedTabPosition
    }

    private fun share(transactionDetailsText: String) {
        startActivity(
            ShareCompat.IntentBuilder.from(this)
                .setType(MIME_TYPE)
                .setChooserTitle(getString(R.string.chucker_share_transaction_title))
                .setSubject(getString(R.string.chucker_share_transaction_subject))
                .setText(transactionDetailsText)
                .createChooserIntent()
        )
    }

    companion object {
        private const val MIME_TYPE = "text/plain"
        private const val EXTRA_TRANSACTION_ID = "transaction_id"
        private var selectedTabPosition = 0

        fun start(context: Context, transactionId: Long) {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            context.startActivity(intent)
        }
    }
}
