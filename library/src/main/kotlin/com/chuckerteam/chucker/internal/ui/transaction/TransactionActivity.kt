package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerActivityTransactionBinding
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.share.TransactionCurlCommandSharable
import com.chuckerteam.chucker.internal.support.share.TransactionDetailsHarSharable
import com.chuckerteam.chucker.internal.support.share.HttpTransactionDetailsSharable
import com.chuckerteam.chucker.internal.support.shareAsFile
import com.chuckerteam.chucker.internal.support.shareAsUtf8Text
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity
import com.chuckerteam.chucker.internal.ui.transaction.event.EventTransactionFragment
import com.chuckerteam.chucker.internal.ui.transaction.http.HttpTransactionFragment
import com.chuckerteam.chucker.internal.ui.transaction.http.HttpTransactionPagerAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class TransactionActivity : BaseChuckerActivity() {

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            intent.getLongExtra(EXTRA_TRANSACTION_ID, 0),
            Transaction.Type.valueOf(
                intent.getStringExtra(EXTRA_TRANSACTION_TYPE) ?: Transaction.Type.Http.name
            )
        )
    }

    private lateinit var transactionBinding: ChuckerActivityTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionBinding = ChuckerActivityTransactionBinding.inflate(layoutInflater)
        with(transactionBinding) {
            setContentView(root)
            val fragment = when (viewModel.transactionType) {
                Transaction.Type.Http -> HttpTransactionFragment()
                Transaction.Type.Event -> EventTransactionFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chucker_transaction, menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        private const val EXTRA_TRANSACTION_ID = "transaction_id"
        private const val EXTRA_TRANSACTION_TYPE = "transaction_type"

        fun start(context: Context, transactionId: Long, transactionType: Transaction.Type) {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            intent.putExtra(EXTRA_TRANSACTION_TYPE, transactionType.name)
            context.startActivity(intent)
        }
    }
}
