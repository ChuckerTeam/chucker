package com.readystatesoftware.chuck.internal.ui.error;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple;
import com.readystatesoftware.chuck.internal.data.repository.ChuckerRepositoryProvider;
import com.readystatesoftware.chuck.internal.support.SQLiteUtils;

import java.util.List;

public class ErrorListFragment extends Fragment {

    private ErrorAdapter adapter;
    private ErrorAdapter.ErrorClickListListener listener;

    public static Fragment newInstance() {
        return new ErrorListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ErrorAdapter.ErrorClickListListener) {
            listener = (ErrorAdapter.ErrorClickListListener) context;
        } else {
            throw new IllegalArgumentException("Context must implement the listener.");
        }
        ChuckerRepositoryProvider.it().getSortedThrowablesTuples().observe(this, new Observer<List<RecordedThrowableTuple>>() {
            @Override
            public void onChanged(@Nullable List<RecordedThrowableTuple> tuples) {
                adapter.setData(tuples);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_error_list, container, false);

        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            adapter = new ErrorAdapter(getContext(), listener);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chuck_errors_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            askForConfirmation();
            return true;
        } else if (item.getItemId() == R.id.browse_sql) {
            SQLiteUtils.browseDatabase(getContext());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void askForConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.chuck_clear)
                .setMessage(R.string.chuck_clear_error_confirmation)
                .setPositiveButton(R.string.chuck_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    ChuckerRepositoryProvider.it().deleteAllThrowables();
                    }
                })
                .setNegativeButton(R.string.chuck_cancel, null)
                .show();
    }
}
