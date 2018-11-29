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
package com.readystatesoftware.chuck.api;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * No-op implementation.
 */
public final class ChuckInterceptor implements Interceptor {

    public ChuckInterceptor(Context context) {
    }

    public ChuckInterceptor(Context context, ChuckCollector collector) {
    }

    public ChuckInterceptor maxContentLength(long max) {
        return this;
    }

    public ChuckInterceptor redactHeader(String name) { return this; }

    public ChuckInterceptor redactHeaders(String... names) { return this; }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }
}
