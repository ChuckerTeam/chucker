/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuckerteam.chucker.api.internal.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.Chucker;
import com.chuckerteam.chucker.api.internal.ui.error.ErrorActivity;
import com.chuckerteam.chucker.api.internal.ui.error.ErrorAdapter;
import com.chuckerteam.chucker.api.internal.ui.transaction.TransactionActivity;
import com.chuckerteam.chucker.api.internal.ui.transaction.TransactionAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends BaseChuckerActivity implements TransactionAdapter.TransactionClickListListener, ErrorAdapter.ErrorClickListListener {

    public static final String EXTRA_SCREEN = "EXTRA_SCREEN";

    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chucker_activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(getApplicationName());

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new HomePageAdapter(this, getSupportFragmentManager()));

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    Chucker.dismissTransactionsNotification(MainActivity.this);
                } else {
                    Chucker.dismissErrorsNotification(MainActivity.this);
                }
            }
        });
        consumeIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        consumeIntent(intent);
    }

    /**
     * Scroll to the right tab.
     */
    private void consumeIntent(Intent intent) {
        // Get the screen to show, by default => HTTP
        int screenToShow = intent.getIntExtra(EXTRA_SCREEN, Chucker.SCREEN_HTTP);
        if (screenToShow == Chucker.SCREEN_HTTP) {
            viewPager.setCurrentItem(HomePageAdapter.SCREEN_HTTP_INDEX);
        } else {
            viewPager.setCurrentItem(HomePageAdapter.SCREEN_ERROR_INDEX);
        }
    }

    private CharSequence getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        return applicationInfo.loadLabel(getPackageManager());
    }

    @Override
    public void onErrorClick(long throwableId, int position) {
        ErrorActivity.start(this, throwableId);
    }

    @Override
    public void onTransactionClick(long transactionId, int position) {
        TransactionActivity.start(this, transactionId);
    }
}
