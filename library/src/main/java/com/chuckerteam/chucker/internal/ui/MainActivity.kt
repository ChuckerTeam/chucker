package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.google.android.material.tabs.TabLayout

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
            viewPager.adapter = HomePageAdapter(this@MainActivity, supportFragmentManager)
            tabLayout.setupWithViewPager(viewPager)
            viewPager.addOnPageChangeListener(
                object : TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == 0) {
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
            HomePageAdapter.SCREEN_ERROR_INDEX
        }
    }

    companion object {
        const val EXTRA_SCREEN = "EXTRA_SCREEN"
    }
}
