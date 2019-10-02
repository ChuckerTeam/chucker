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
package com.chuckerteam.chucker.internal.ui.traffic.http;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.internal.support.FormatUtils;
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class TransactionActivity extends BaseChuckerActivity {

    private static final String ARG_TRANSACTION_ID = "transaction_id";
    private static int selectedTabPosition = 0;

    public static void start(Context context, long transactionId) {
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra(ARG_TRANSACTION_ID, transactionId);
        context.startActivity(intent);
    }

    TextView title;
    PagerAdapter adapter;

    private TransactionViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chucker_activity_transaction);

        // Create the instance now, so it can be shared by the
        // various fragments in the view pager later.
        long transactionId = getIntent().getLongExtra(ARG_TRANSACTION_ID, 0);
        viewModel = ViewModelProviders
                .of(this, new TransactionViewModelFactory(transactionId))
                .get(TransactionViewModel.class);
        viewModel.loadTransaction();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewPager viewPager = findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getTransactionTitle().observe(this, s -> title.setText(s));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chucker_transaction, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_text) {
            share(FormatUtils.getShareText(this, Objects.requireNonNull(
                    viewModel.getTransaction$library_debug().getValue())));
            return true;
        } else if (item.getItemId() == R.id.share_curl) {
            share(FormatUtils.getShareCurlCommand(Objects.requireNonNull(
                    viewModel.getTransaction$library_debug().getValue())));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new PagerAdapter(getApplicationContext(), getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                selectedTabPosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }

        });
        viewPager.setCurrentItem(selectedTabPosition);
    }

    private void share(String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    class PagerAdapter extends FragmentStatePagerAdapter {
        private final WeakReference<Context> context;
        private final int[] TITLE_RES_IDS = {
                R.string.chucker_overview,
                R.string.chucker_request,
                R.string.chucker_response
        };

        PagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = new WeakReference<>(context);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TransactionOverviewFragment();
                case 1:
                    return TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_REQUEST);
                case 2:
                    return TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_RESPONSE);
                default:
                    throw new IllegalArgumentException("no item");
            }
        }

        @Override
        public int getCount() {
            return TITLE_RES_IDS.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            Context context = this.context.get();
            if (context == null) {
                return null;
            }
            return context.getString(TITLE_RES_IDS[position]);
        }
    }
}
