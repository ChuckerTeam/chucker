package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.R

internal class TransactionPagerAdapter(context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var cookiesPresent: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response),
        context.getString(R.string.chucker_cookies)
    )

    override fun getItem(position: Int): Fragment = when (position) {
        OVERVIEW -> TransactionOverviewFragment()
        REQUEST -> TransactionPayloadFragment.newInstance(PayloadType.REQUEST)
        RESPONSE -> TransactionPayloadFragment.newInstance(PayloadType.RESPONSE)
        COOKIES -> TransactionCookiesFragment()
        else -> throw IllegalArgumentException("no item")
    }

    override fun getCount(): Int = if (cookiesPresent) titles.size else titles.size - 1

    override fun getPageTitle(position: Int): CharSequence? = titles[position]

    companion object {
        const val OVERVIEW = 0
        const val REQUEST = 1
        const val RESPONSE = 2
        const val COOKIES = 3
    }
}
