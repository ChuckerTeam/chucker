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
package com.chuckerteam.chucker.internal.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider;

public abstract class BaseChuckerActivity extends AppCompatActivity {

    private static boolean inForeground;

    public static boolean isInForeground() {
        return inForeground;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RepositoryProvider.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inForeground = false;
    }
}
