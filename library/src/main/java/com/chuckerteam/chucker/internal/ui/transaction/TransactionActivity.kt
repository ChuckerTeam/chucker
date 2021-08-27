package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.databinding.ChuckerActivityTransactionBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.*
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.TransactionCurlCommandSharable
import com.chuckerteam.chucker.internal.support.TransactionDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.shareAsUtf8Text
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

internal class TransactionActivity : BaseChuckerActivity() {

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(intent.getLongExtra(EXTRA_TRANSACTION_ID, 0))
    }

    private lateinit var transactionBinding: ChuckerActivityTransactionBinding

    private val repeatRequestCallback: Callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.message?.let { it1 -> Log.d(viewModel.transaction.value?.url.toString(), it1) }
            runOnUiThread {
                showToast(getString(R.string.chucker_request_failed))
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { it1 -> Log.d(viewModel.transaction.value?.url.toString(), it1) }
            runOnUiThread {
                showToast(getString(R.string.chucker_request_complete))
            }
        }
    }

    private val client: OkHttpClient by lazy {
        val collector = ChuckerCollector(
            context = applicationContext,
            showNotification = true,
            retentionPeriod = PrefUtils.getInstance(this).getRetentionPeriod()
        )

        val redactedHeaders = PrefUtils.getInstance(this).getRedactedHeaders()

        val chuckerInterceptor = ChuckerInterceptor.Builder(applicationContext)
            .collector(collector)
            .maxContentLength(250_000L)
            .redactHeaders(redactedHeaders)
            .build()

        OkHttpClient.Builder()
            .addNetworkInterceptor(chuckerInterceptor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionBinding = ChuckerActivityTransactionBinding.inflate(layoutInflater)

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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share_text -> shareTransactionAsText { transaction ->
            val encodeUrls = viewModel.encodeUrl.value!!
            TransactionDetailsSharable(transaction, encodeUrls)
        }
        R.id.share_curl -> shareTransactionAsText { transaction ->
            TransactionCurlCommandSharable(transaction)
        }
        R.id.share_file -> shareTransactionAsFile { transaction ->
            val encodeUrls = viewModel.encodeUrl.value!!
            TransactionDetailsSharable(transaction, encodeUrls)
        }
        R.id.repeat_request -> {
            viewModel.repeatRequest(client, repeatRequestCallback)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun shareTransactionAsText(block: (HttpTransaction) -> Sharable): Boolean {
        val transaction = viewModel.transaction.value
        if (transaction == null) {
            showToast(getString(R.string.chucker_request_not_ready))
            return true
        }
        val sharable = block(transaction)
        lifecycleScope.launch {
            val shareIntent = sharable.shareAsUtf8Text(
                activity = this@TransactionActivity,
                intentTitle = getString(R.string.chucker_share_transaction_title),
                intentSubject = getString(R.string.chucker_share_transaction_subject)
            )
            startActivity(shareIntent)
        }
        return true
    }

    private fun shareTransactionAsFile(block: (HttpTransaction) -> Sharable): Boolean {
        val transaction = viewModel.transaction.value
        if (transaction == null) {
            showToast(getString(R.string.chucker_request_not_ready))
            return true
        }

        val sharable = block(transaction)
        lifecycleScope.launch {
            val shareIntent = sharable.shareAsFile(
                activity = this@TransactionActivity,
                fileName = EXPORT_FILE_NAME,
                intentTitle = getString(R.string.chucker_share_transaction_title),
                intentSubject = getString(R.string.chucker_share_transaction_subject),
                clipDataLabel = "transaction"
            )
            if (shareIntent != null) {
                startActivity(shareIntent)
            }
        }
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = TransactionPagerAdapter(this, supportFragmentManager)
        viewPager.addOnPageChangeListener(
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    selectedTabPosition = position
                }
            }
        )
        viewPager.currentItem = selectedTabPosition
    }

    companion object {
        private const val EXPORT_FILE_NAME = "transaction.txt"
        private const val EXTRA_TRANSACTION_ID = "transaction_id"
        private var selectedTabPosition = 0

        fun start(context: Context, transactionId: Long) {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            context.startActivity(intent)
        }
    }
}
