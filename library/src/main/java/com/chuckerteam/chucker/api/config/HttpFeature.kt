package com.chuckerteam.chucker.api.config

import android.content.Context
import androidx.fragment.app.Fragment
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.internal.ui.transaction.TransactionListFragment

class HttpFeature(
    override val enabled: Boolean,
    val showNotification: Boolean,
    val retentionPeriod: RetentionManager.Period
) : Feature {
    override val name: Int = R.string.chucker_tab_network

    override val tag: Int = Chucker.SCREEN_ERROR

    override fun newFragment(): Fragment {
        return TransactionListFragment.newInstance()
    }

    override fun dismissNotification(context: Context) {
        Chucker.dismissTransactionsNotification(context)
    }
}
