package com.chuckerteam.chucker.internal.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chuckerteam.chucker.internal.ui.error.ErrorListFragment
import com.chuckerteam.chucker.internal.ui.transaction.TransactionListFragment

internal class HomePageAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment = if (position == NETWORK_SCREEN_POSITION) {
        TransactionListFragment.newInstance()
    } else {
        ErrorListFragment.newInstance()
    }

    override fun getItemCount(): Int = HOME_SCREENS_NUMBER

    companion object {
        private const val HOME_SCREENS_NUMBER = 2
        internal const val NETWORK_SCREEN_POSITION = 0
        internal const val ERROR_SCREEN_POSITION = 1
    }
}
