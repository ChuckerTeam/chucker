package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.ui.error.ErrorActivity
import com.chuckerteam.chucker.internal.ui.error.ErrorAdapter
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

internal class MainActivity :
    BaseChuckerActivity(),
    TransactionAdapter.TransactionClickListListener,
    ErrorAdapter.ErrorClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var viewPager: ViewPager2

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.subtitle = applicationName

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setupViewPager()

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
        viewPager.currentItem = if (screenToShow == Chucker.SCREEN_HTTP) {
            HomePageAdapter.NETWORK_SCREEN_POSITION
        } else {
            HomePageAdapter.ERROR_SCREEN_POSITION
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPagerHome)

        viewPager.adapter = HomePageAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == HomePageAdapter.NETWORK_SCREEN_POSITION) {
                    Chucker.dismissTransactionsNotification(this@MainActivity)
                } else {
                    Chucker.dismissErrorsNotification(this@MainActivity)
                }
            }
        })

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHome)
        TabLayoutMediator(tabLayout, viewPager) { currentTab, currentPosition ->
            currentTab.text = if (currentPosition == HomePageAdapter.NETWORK_SCREEN_POSITION) {
                getString(R.string.tab_network)
            } else {
                getString(R.string.tab_errors)
            }
        }.attach()
    }

    override fun onErrorClick(throwableId: Long, position: Int) {
        ErrorActivity.start(this, throwableId)
    }

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(this, transactionId)
    }

    companion object {
        const val EXTRA_SCREEN = "EXTRA_SCREEN"
    }
}
