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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.google.android.material.tabs.TabLayout

internal class TransactionActivity : BaseChuckerActivity() {
    private lateinit var title: TextView
    private lateinit var adapter: TransactionPagerAdapter

    private var selectedTabPosition = 0
    private var transactionId: Long = 0
    private var transaction: HttpTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chucker_activity_transaction)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = findViewById(R.id.toolbar_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        if (viewPager != null) {
            setupViewPager(viewPager)
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, 0)
    }

    override fun onResume() {
        super.onResume()
        RepositoryProvider.transaction().getTransaction(transactionId)
            .observe(
                this,
                Observer { observed ->
                    transaction = observed
                    populateUI(observed)
                }
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.chucker_transaction, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.share_text -> {
            share(FormatUtils.getShareText(this, transaction!!))
            true
        }
        R.id.share_curl -> {
            share(FormatUtils.getShareCurlCommand(transaction!!))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun populateUI(transaction: HttpTransaction?) {
        if (transaction != null) {
            title.text = "${transaction.method} ${transaction.path}"
            for (fragment in supportFragmentManager.fragments) {
                if (fragment is TransactionFragment) {
                    (fragment as TransactionFragment).transactionUpdated(transaction)
                }
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        adapter = TransactionPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                selectedTabPosition = position
                populateUI(transaction)
            }
        })
        viewPager.currentItem = selectedTabPosition
    }

    private fun share(content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    companion object {
        private const val EXTRA_TRANSACTION_ID = "transaction_id"

        fun start(context: Context, transactionId: Long) {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            context.startActivity(intent)
        }
    }
}
