package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareCurlCommand
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareText
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

internal class TransactionActivity : BaseChuckerActivity() {

    private lateinit var title: TextView
    private lateinit var viewPagerAdapter: TransactionPagerAdapter
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0)

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        viewModel = ViewModelProvider(this, TransactionViewModelFactory(transactionId))
            .get(TransactionViewModel::class.java)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = findViewById(R.id.toolbar_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewPager()
    }

    override fun onResume() {
        super.onResume()
        viewModel.transactionTitle.observe(
            this,
            Observer { title.text = it }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.transaction, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.share_text -> {
                viewModel.transaction.value?.let {
                    share(getShareText(this, it))
                } ?: showToast(getString(R.string.request_not_ready))
                true
            }
            R.id.share_curl -> {
                viewModel.transaction.value?.let {
                    share(getShareCurlCommand(it))
                } ?: showToast(getString(R.string.request_not_ready))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = findViewById(R.id.viewPagerTransaction)

        viewPagerAdapter = TransactionPagerAdapter(this)
        viewPager.apply {
            adapter = viewPagerAdapter
            offscreenPageLimit = TransactionPagerAdapter.TRANSACTION_SCREEN_OFFSCREEN_LIMIT as Int
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    selectedTabPosition = position
                }
            })
            currentItem = selectedTabPosition
        }

        val tabLayout: TabLayout = findViewById(R.id.tabLayoutTransaction)
        TabLayoutMediator(tabLayout, viewPager) { currentTab, currentPosition ->
            currentTab.text = when (currentPosition) {
                TransactionPagerAdapter.OVERVIEW_SCREEN_POSITION -> getString(R.string.overview)
                TransactionPagerAdapter.REQUEST_SCREEN_POSITION -> getString(R.string.request)
                else -> getString(R.string.response)
            }
        }.attach()
    }

    private fun share(transactionDetailsText: String) {
        startActivity(
            ShareCompat.IntentBuilder.from(this)
                .setType(MIME_TYPE)
                .setChooserTitle(getString(R.string.share_transaction_title))
                .setSubject(getString(R.string.share_transaction_subject))
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
