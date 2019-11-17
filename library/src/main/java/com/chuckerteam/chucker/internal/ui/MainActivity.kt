/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.ui.error.ErrorActivity
import com.chuckerteam.chucker.internal.ui.error.ErrorAdapter
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity :
    BaseChuckerActivity(),
    TransactionAdapter.TransactionClickListListener,
    ErrorAdapter.ErrorClickListListener {
    private lateinit var viewPager: ViewPager2

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.subtitle = applicationName

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
        viewPager.adapter = HomePageAdapter(supportFragmentManager, lifecycle)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHome)
        TabLayoutMediator(tabLayout, viewPager) { currentTab, currentPosition ->
            currentTab.text = if (currentPosition == HomePageAdapter.NETWORK_SCREEN_POSITION) {
                getString(R.string.chucker_tab_network)
            } else {
                getString(R.string.chucker_tab_errors)
            }
        }.attach()

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
