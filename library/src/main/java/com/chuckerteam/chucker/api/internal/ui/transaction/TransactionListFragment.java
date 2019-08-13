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
package com.chuckerteam.chucker.api.internal.ui.transaction;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransactionTuple;
import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider;
import com.chuckerteam.chucker.api.internal.support.NotificationHelper;

import java.util.List;

public class TransactionListFragment extends Fragment implements SearchView.OnQueryTextListener, TransactionAdapter.TransactionClickListListener, Observer<List<HttpTransactionTuple>> {

    private String currentFilter = "";
    private TransactionAdapter adapter;
    LiveData<List<HttpTransactionTuple>> dataSource;

    public static TransactionListFragment newInstance() {
        return new TransactionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chucker_fragment_transaction_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));
            adapter = new TransactionAdapter(getContext(), this);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataSource = getDataSource(currentFilter);
        dataSource.observe(this, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chucker_transactions_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            askForConfirmation();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void askForConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.chucker_clear)
                .setMessage(R.string.chucker_clear_http_confirmation)
                .setPositiveButton(R.string.chucker_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RepositoryProvider.transaction().deleteAllTransactions();
                        NotificationHelper.clearBuffer();
                    }
                })
                .setNegativeButton(R.string.chucker_cancel, null)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentFilter = newText;
        dataSource.removeObservers(this);
        dataSource = getDataSource(currentFilter);
        dataSource.observe(this, this);
        return true;
    }

    private LiveData<List<HttpTransactionTuple>> getDataSource(String currentFilter) {
        if (currentFilter.isEmpty()){
            return RepositoryProvider.transaction().getSortedTransactionTuples();
        } else if (TextUtils.isDigitsOnly(currentFilter)){
            return RepositoryProvider.transaction().getFilteredTransactionTuples(currentFilter, "");
        } else {
            return RepositoryProvider.transaction().getFilteredTransactionTuples("", currentFilter);
        }
    }

    @Override
    public void onChanged(@Nullable List<HttpTransactionTuple> tuples) {
        adapter.setData(tuples);
    }

    @Override
    public void onTransactionClick(long transactionId, int position) {
        TransactionActivity.start(getActivity(), transactionId);
    }
}
