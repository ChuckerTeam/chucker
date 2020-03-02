package com.chuckerteam.chucker.internal.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.ui.throwable.ThrowableListFragment
import com.chuckerteam.chucker.internal.ui.transaction.TransactionListFragment
import java.lang.ref.WeakReference

internal class HomePageAdapter(context: Context, fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val context: WeakReference<Context> = WeakReference(context)

    override fun getItem(position: Int): Fragment = if (position == SCREEN_HTTP_INDEX) {
        TransactionListFragment.newInstance()
    } else {
        ThrowableListFragment.newInstance()
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? =
        context.get()?.getString(
            if (position == SCREEN_HTTP_INDEX) {
                R.string.chucker_tab_network
            } else {
                R.string.chucker_tab_errors
            }
        )

    companion object {
        const val SCREEN_HTTP_INDEX = 0
        const val SCREEN_THROWABLE_INDEX = 1
    }
}
