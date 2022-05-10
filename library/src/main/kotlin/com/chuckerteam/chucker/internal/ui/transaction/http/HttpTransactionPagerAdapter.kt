package com.chuckerteam.chucker.internal.ui.transaction.http

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.R

internal class HttpTransactionPagerAdapter(context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response)
    )

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> HttpTransactionOverviewFragment()
        1 -> HttpTransactionPayloadFragment.newInstance(HttpPayloadType.REQUEST)
        2 -> HttpTransactionPayloadFragment.newInstance(HttpPayloadType.RESPONSE)
        else -> throw IllegalArgumentException("no item")
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence = titles[position]
}
