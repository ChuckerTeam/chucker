package com.chuckerteam.chucker.internal.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chuckerteam.chucker.internal.ui.error.ErrorListFragment
import com.chuckerteam.chucker.internal.ui.transaction.TransactionListFragment

internal class HomePageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment = if (position == SCREEN_NETWORK_INDEX) {
        TransactionListFragment.newInstance()
    } else {
        ErrorListFragment.newInstance()
    }

    override fun getItemCount(): Int = HOME_SCREENS_NUMBER

    companion object {
        private const val HOME_SCREENS_NUMBER = 2
        const val SCREEN_NETWORK_INDEX = 0
        const val SCREEN_ERROR_INDEX = 1
    }
}
