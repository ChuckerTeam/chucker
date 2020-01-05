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
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareCurlCommand
import com.chuckerteam.chucker.internal.support.FormatUtils.getShareText
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.google.android.material.tabs.TabLayout

class TransactionActivity : BaseChuckerActivity() {

    private lateinit var title: TextView
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_transaction)

        val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0)

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        viewModel = ViewModelProviders
            .of(this, TransactionViewModelFactory(transactionId))
            .get(TransactionViewModel::class.java)
        viewModel.loadTransaction()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = findViewById(R.id.toolbar_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<ViewPager>(R.id.viewpager)?.let { viewPager ->
            setupViewPager(viewPager)
            findViewById<TabLayout>(R.id.tabs).setupWithViewPager(viewPager)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.transactionTitle.observe(
            this,
            Observer { title.text = it }
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

        @JvmStatic
        fun start(context: Context, transactionId: Long) {
            context.startActivity(
                Intent(context, TransactionActivity::class.java).apply {
                    putExtra(EXTRA_TRANSACTION_ID, transactionId)
                }
            )
        }
    }
}
