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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chuckerteam.chucker.R;
import com.chuckerteam.chucker.api.internal.data.entity.HttpTransaction;

import org.jetbrains.annotations.NotNull;

public class TransactionOverviewFragment extends Fragment implements TransactionFragment {

    TextView url;
    TextView method;
    TextView protocol;
    TextView status;
    TextView response;
    TextView ssl;
    TextView requestTime;
    TextView responseTime;
    TextView duration;
    TextView requestSize;
    TextView responseSize;
    TextView totalSize;

    private HttpTransaction transaction;

    public TransactionOverviewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chucker_fragment_transaction_overview, container, false);
        url = view.findViewById(R.id.url);
        method = view.findViewById(R.id.method);
        protocol = view.findViewById(R.id.protocol);
        status = view.findViewById(R.id.status);
        response = view.findViewById(R.id.response);
        ssl = view.findViewById(R.id.ssl);
        requestTime = view.findViewById(R.id.request_time);
        responseTime = view.findViewById(R.id.response_time);
        duration = view.findViewById(R.id.duration);
        requestSize = view.findViewById(R.id.request_size);
        responseSize = view.findViewById(R.id.response_size);
        totalSize = view.findViewById(R.id.total_size);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateUI();
    }

    @Override
    public void transactionUpdated(HttpTransaction transaction) {
        this.transaction = transaction;
        populateUI();
    }

    private void populateUI() {
        if (isAdded() && transaction != null) {
            url.setText(transaction.getUrl());
            method.setText(transaction.getMethod());
            protocol.setText(transaction.getProtocol());
            status.setText(transaction.getStatus().toString());
            response.setText(transaction.getResponseSummaryText());
            ssl.setText((transaction.isSsl() ? R.string.chucker_yes : R.string.chucker_no));
            requestTime.setText(transaction.getRequestDateString());
            responseTime.setText(transaction.getResponseDateString());
            duration.setText(transaction.getDurationString());
            requestSize.setText(transaction.getRequestSizeString());
            responseSize.setText(transaction.getResponseSizeString());
            totalSize.setText(transaction.getTotalSizeString());
        }
    }
}
