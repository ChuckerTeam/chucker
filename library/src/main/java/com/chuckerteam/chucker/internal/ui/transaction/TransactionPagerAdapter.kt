package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.R

internal class TransactionPagerAdapter(context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response)
    )

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> TransactionOverviewFragment()
        1 -> TransactionPayloadFragment.newInstance(PayloadType.Request)
        2 -> TransactionPayloadFragment.newInstance(PayloadType.Response)
        else -> throw IllegalArgumentException("no item")
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? = titles[position]
}
