package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.ui.throwable.ThrowableActivity
import com.chuckerteam.chucker.internal.ui.throwable.ThrowableAdapter
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import com.google.android.material.tabs.TabLayout

internal class MainActivity :
    BaseChuckerActivity(),
    TransactionAdapter.TransactionClickListListener,
    ThrowableAdapter.ThrowableClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var mainBinding: ChuckerActivityMainBinding

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainBinding = ChuckerActivityMainBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName
            viewPager.adapter = HomePageAdapter(this@MainActivity, supportFragmentManager)
            tabLayout.setupWithViewPager(viewPager)
            viewPager.addOnPageChangeListener(
                object : TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == HomePageAdapter.SCREEN_HTTP_INDEX) {
                            Chucker.dismissTransactionsNotification(this@MainActivity)
                        } else {
                            Chucker.dismissErrorsNotification(this@MainActivity)
                        }
                    }
                }
            )
        }

        consumeIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeIntent(intent)
    }

    /**
     * Scroll to the right tab.
     */
    private fun consumeIntent(intent: Intent) {
        // Get the screen to show, by default => HTTP
        val screenToShow = intent.getIntExtra(EXTRA_SCREEN, Chucker.SCREEN_HTTP)
        mainBinding.viewPager.currentItem = if (screenToShow == Chucker.SCREEN_HTTP) {
            HomePageAdapter.SCREEN_HTTP_INDEX
        } else {
            HomePageAdapter.SCREEN_THROWABLE_INDEX
        }
    }

    override fun onThrowableClick(throwableId: Long, position: Int) {
        ThrowableActivity.start(this, throwableId)
    }

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(this, transactionId)
    }

    companion object {
        const val EXTRA_SCREEN = "EXTRA_SCREEN"
    }
}
