package com.chuckerteam.chucker.api.config

import android.content.Context
import androidx.fragment.app.Fragment
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.api.dsl.DEFAULT_MAX_CONTENT_LENGTH
import com.chuckerteam.chucker.internal.ui.transaction.TransactionListFragment

class HttpFeature(
    override var enabled: Boolean,
    var showNotification: Boolean,
    var retentionPeriod: RetentionManager.Period,
    var maxContentLength: Long,
    var headersToRedact: MutableSet<String>
) : TabFeature {
    override val name: Int = R.string.chucker_tab_network

    override val id: Int = Chucker.SCREEN_HTTP

    override fun newFragment(): Fragment {
        return TransactionListFragment.newInstance()
    }

    override fun dismissNotification(context: Context) {
        Chucker.dismissTransactionsNotification(context)
    }

    companion object {
        fun default(): HttpFeature =
            HttpFeature(
                enabled = true,
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_WEEK,
                headersToRedact = mutableSetOf(),
                maxContentLength = DEFAULT_MAX_CONTENT_LENGTH
            )
    }
}
