package com.chuckerteam.chucker.api.internal.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.ui.error.ErrorListFragment;
import com.chuckerteam.chucker.api.internal.ui.transaction.TransactionListFragment;

/**
 * @author Olivier Perez
 */
class HomePageAdapter extends FragmentPagerAdapter {

    static final int SCREEN_HTTP_INDEX = 0;
    static final int SCREEN_ERROR_INDEX = 1;

    private final Context context;

     HomePageAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == SCREEN_HTTP_INDEX) {
            return TransactionListFragment.newInstance();
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
        if (position == SCREEN_HTTP_INDEX) {
            return context.getString(R.string.chucker_tab_network);
        } else {
            return context.getString(R.string.chucker_tab_errors);
        }
    }
}
