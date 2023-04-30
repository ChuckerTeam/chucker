package com.chuckerteam.chucker.internal.ui.transaction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chuckerteam.chucker.R

internal class TransactionPagerAdapter(activity: TransactionActivity) :
    FragmentStateAdapter(activity) {

    internal val titles = arrayOf(
        activity.getString(R.string.chucker_overview),
        activity.getString(R.string.chucker_request),
        activity.getString(R.string.chucker_response)
    )

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> TransactionOverviewFragment()
        1 -> TransactionPayloadFragment.newInstance(PayloadType.REQUEST)
        2 -> TransactionPayloadFragment.newInstance(PayloadType.RESPONSE)
        else -> throw IllegalArgumentException("no item")
    }
}
