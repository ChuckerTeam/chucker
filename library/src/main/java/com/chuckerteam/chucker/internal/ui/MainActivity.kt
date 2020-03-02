package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

internal class MainActivity :
    BaseChuckerActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var mainBinding: ChuckerActivityMainBinding

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ChuckerActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName
        }

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
        mainBinding.viewPager.currentItem = if (screenToShow == Chucker.SCREEN_HTTP) {
            HomePageAdapter.NETWORK_SCREEN_POSITION
        } else {
            HomePageAdapter.THROWABLE_SCREEN_POSITION
        }
    }

    private fun setupViewPager() {
        mainBinding.viewPager.apply {
            adapter = HomePageAdapter(this@MainActivity)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == HomePageAdapter.NETWORK_SCREEN_POSITION) {
                        Chucker.dismissTransactionsNotification(this@MainActivity)
                    } else {
                        Chucker.dismissErrorsNotification(this@MainActivity)
                    }
                }
            })
        }

        TabLayoutMediator(mainBinding.tabLayout, mainBinding.viewPager) { currentTab, currentPosition ->
            currentTab.text = if (currentPosition == HomePageAdapter.NETWORK_SCREEN_POSITION) {
                getString(R.string.chucker_tab_network)
            } else {
                getString(R.string.chucker_tab_errors)
            }
        }.attach()
    }

    companion object {
        const val EXTRA_SCREEN = "EXTRA_SCREEN"
    }
}
