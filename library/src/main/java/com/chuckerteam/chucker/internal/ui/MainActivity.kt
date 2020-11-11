package com.chuckerteam.chucker.internal.ui

import android.os.Bundle
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter

internal class MainActivity :
    BaseChuckerActivity(),
    TransactionAdapter.TransactionClickListListener {

    private lateinit var mainBinding: ChuckerActivityMainBinding

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ChuckerActivityMainBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName
        }
    }

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(this, transactionId)
    }
}
