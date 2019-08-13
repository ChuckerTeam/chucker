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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction;
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransactionTuple;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final TransactionClickListListener listener;
    private List<HttpTransactionTuple> transactions;

    private final int colorDefault;
    private final int colorRequested;
    private final int colorError;
    private final int color500;
    private final int color400;
    private final int color300;


    TransactionAdapter(Context context, TransactionClickListListener listener) {
        this.listener = listener;
        this.transactions = new ArrayList<>();
        colorDefault = ContextCompat.getColor(context, R.color.chucker_status_default);
        colorRequested = ContextCompat.getColor(context, R.color.chucker_status_requested);
        colorError = ContextCompat.getColor(context, R.color.chucker_status_error);
        color500 = ContextCompat.getColor(context, R.color.chucker_status_500);
        color400 = ContextCompat.getColor(context, R.color.chucker_status_400);
        color300 = ContextCompat.getColor(context, R.color.chucker_status_300);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chucker_list_item_transaction, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HttpTransactionTuple transaction = transactions.get(position);
        holder.bind(transaction);
    }

    public void setData(List<HttpTransactionTuple> httpTransactions) {
        this.transactions = httpTransactions;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView code;
        final TextView path;
        final TextView host;
        final TextView start;
        final TextView duration;
        final TextView size;
        final ImageView ssl;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            code = view.findViewById(R.id.chucker_code);
            path = view.findViewById(R.id.chucker_path);
            host = view.findViewById(R.id.chucker_host);
            start = view.findViewById(R.id.chucker_time_start);
            duration = view.findViewById(R.id.chucker_duration);
            size = view.findViewById(R.id.chucker_size);
            ssl = view.findViewById(R.id.chucker_ssl);
        }

        void bind(final HttpTransactionTuple transaction) {
            path.setText(String.format("%s %s", transaction.getMethod(), transaction.getPath()));
            host.setText(transaction.getHost());
            start.setText(DateFormat.getTimeInstance().format(transaction.getRequestDate()));
            ssl.setVisibility(transaction.isSsl() ? View.VISIBLE : View.GONE);
            if (transaction.getStatus() == HttpTransaction.Status.Complete) {
                code.setText(String.valueOf(transaction.getResponseCode()));
                duration.setText(transaction.getDurationString());
                size.setText(transaction.getTotalSizeString());
            } else {
                code.setText("");
                duration.setText("");
                size.setText("");
            }
            if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                code.setText("!!!");
            }
            setStatusColor(this, transaction);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTransactionClick(transaction.getId(), getAdapterPosition());
                    }
                }
            });
        }

        private void setStatusColor(ViewHolder holder, HttpTransactionTuple transaction) {
            int color;
            if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                color = colorError;
            } else if (transaction.getStatus() == HttpTransaction.Status.Requested) {
                color = colorRequested;
            } else if (transaction.getResponseCode() == null) {
                color = colorDefault;
            } else if (transaction.getResponseCode() >= 500) {
                color = color500;
            } else if (transaction.getResponseCode() >= 400) {
                color = color400;
            } else if (transaction.getResponseCode() >= 300) {
                color = color300;
            } else {
                color = colorDefault;
            }
            holder.code.setTextColor(color);
            holder.path.setTextColor(color);
        }
    }

    public interface TransactionClickListListener {
        void onTransactionClick(long transactionId, int position);
    }
}
