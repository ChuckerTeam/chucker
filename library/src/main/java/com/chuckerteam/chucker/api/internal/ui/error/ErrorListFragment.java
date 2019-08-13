package com.chuckerteam.chucker.api.internal.ui.error;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.RecordedThrowableTuple;
import com.chuckerteam.chucker.api.internal.data.repository.RepositoryProvider;

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
        RepositoryProvider.throwable().getSortedThrowablesTuples().observe(this, new Observer<List<RecordedThrowableTuple>>() {
            @Override
            public void onChanged(@Nullable List<RecordedThrowableTuple> tuples) {
                adapter.setData(tuples);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chucker_fragment_error_list, container, false);

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
        inflater.inflate(R.menu.chucker_errors_list, menu);
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
                .setMessage(R.string.chucker_clear_error_confirmation)
                .setPositiveButton(R.string.chucker_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    RepositoryProvider.throwable().deleteAllThrowables();
                    }
                })
                .setNegativeButton(R.string.chucker_cancel, null)
                .show();
    }
}
