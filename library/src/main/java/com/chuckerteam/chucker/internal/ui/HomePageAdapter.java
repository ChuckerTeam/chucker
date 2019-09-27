package com.chuckerteam.chucker.internal.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.internal.ui.error.ErrorListFragment;
import com.chuckerteam.chucker.internal.ui.traffic.TrafficFragment;

import java.lang.ref.WeakReference;

/**
 * @author Olivier Perez
 */
class HomePageAdapter extends FragmentStatePagerAdapter {

    static final int SCREEN_HTTP_INDEX = 0;
    static final int SCREEN_ERROR_INDEX = 1;

    private final WeakReference<Context> context;

    HomePageAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == SCREEN_HTTP_INDEX) {
            return new TrafficFragment();
        } else {
            return ErrorListFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Context context = this.context.get();
        if (context == null) {
            return null;
        }
        if (position == SCREEN_HTTP_INDEX) {
            return context.getString(R.string.chucker_tab_network);
        } else {
            return context.getString(R.string.chucker_tab_errors);
        }
    }
}
