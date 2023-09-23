package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chuckerteam.chucker.R

internal class TransactionPagerAdapter(
    context: Context,
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    private val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response),
    )

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> TransactionOverviewFragment()
        1 -> TransactionPayloadFragment.newInstance(PayloadType.REQUEST)
        2 -> TransactionPayloadFragment.newInstance(PayloadType.RESPONSE)
        else -> throw IllegalArgumentException("no item")
    }

    override fun getItemCount(): Int = titles.size

    fun getPageTitle(position: Int): CharSequence = titles[position]
}
