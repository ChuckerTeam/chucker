package com.chuckerteam.chucker.internal.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.internal.support.FeatureManager
import java.lang.ref.WeakReference

internal class HomePageAdapter(context: Context, fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val context: WeakReference<Context> = WeakReference(context)

    override fun getItem(position: Int): Fragment =
        FeatureManager.getAt(position).newFragment()

    override fun getCount(): Int = FeatureManager.countEnabledFeatures()

    override fun getPageTitle(position: Int): CharSequence? =
        context.get()?.getString(FeatureManager.getAt(position).name)

    companion object {
        const val SCREEN_HTTP_INDEX = 0
        const val SCREEN_ERROR_INDEX = 1
    }
}
