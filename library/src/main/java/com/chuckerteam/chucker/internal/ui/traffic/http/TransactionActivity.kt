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
package com.chuckerteam.chucker.internal.ui.traffic.http

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.getShareCurlCommand
import com.chuckerteam.chucker.internal.support.getShareText
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.google.android.material.tabs.TabLayout

class TransactionActivity : BaseChuckerActivity() {

    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_transaction)

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        val transactionId = intent.getLongExtra(ARG_TRANSACTION_ID, 0)
        viewModel = ViewModelProviders
            .of(this, TransactionViewModelFactory(transactionId))
            .get(TransactionViewModel::class.java)
        viewModel.loadTransaction()

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<ViewPager>(R.id.viewpager)?.let { viewPager ->
            viewPager.adapter = PagerAdapter(applicationContext, supportFragmentManager)
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(pos: Int, offset: Float, pixels: Int) = Unit
                override fun onPageScrollStateChanged(state: Int) = Unit
                override fun onPageSelected(position: Int) {
                    selectedTabPosition = position
                }
            })
            viewPager.currentItem = selectedTabPosition
            findViewById<TabLayout>(R.id.tabs).setupWithViewPager(viewPager)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.transactionTitle.observe(
            this,
            Observer {
                findViewById<TextView>(R.id.toolbar_title).text = it
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chucker_transaction, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.share_text -> {
                share(getShareText(this, viewModel.transaction.value!!))
                true
            }
            R.id.share_curl -> {
                share(getShareCurlCommand(viewModel.transaction.value!!))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun share(content: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }

    internal inner class PagerAdapter(context: Context, fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val titles = arrayOf(
            context.getString(R.string.chucker_overview),
            context.getString(R.string.chucker_request),
            context.getString(R.string.chucker_response)
        )

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> TransactionOverviewFragment()
            1 -> TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_REQUEST)
            2 -> TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_RESPONSE)
            else -> throw IllegalArgumentException("no item")
        }

        override fun getCount(): Int = titles.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"
        private var selectedTabPosition = 0

        fun start(context: Context, transactionId: Long) {
            context.startActivity(
                Intent(context, TransactionActivity::class.java).apply {
                    putExtra(ARG_TRANSACTION_ID, transactionId)
                }
            )
        }
    }
}
