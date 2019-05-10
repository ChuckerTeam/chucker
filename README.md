# Chucker

[![JitPack](https://jitpack.io/v/ChuckerTeam/Chucker.svg)](https://jitpack.io/#ChuckerTeam/Chucker) [![Build Status](https://travis-ci.org/ChuckerTeam/chucker.svg?branch=master)](https://travis-ci.org/ChuckerTeam/chucker) ![License](https://img.shields.io/github/license/ChuckerTeam/Chucker.svg) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-orange.svg)](http://makeapullrequest.com)

<p align="center">
  <img src="https://i.imgur.com/GXoIFZ6.png" alt="chucker icon" width="30%"/>
</p>

_A fork of [Chuck](https://github.com/jgilfelt/chuck)_

Chucker simplifies the gathering of HTTP requests/responses, and Throwables. Chucker intercepts and persists all this events inside your application, and provides an UI for inspecting and sharing their content.

![Chucker HTTP transactions](assets/chucker-http.gif) ![Chucker errors](assets/chucker-error.gif)

Apps using Chucker will display a notifications showing a summary of ongoing HTTP activity and Throwables. Tapping on the notification launches the full Chucker UI. Apps can optionally suppress the notification, and launch the Chucker UI directly from within their own interface.

The main Chucker activity is launched in its own task, allowing it to be displayed alongside the host app UI using Android 7.x multi-window support.

![Multi-Window](assets/chucker-multiwindow.gif)

Chucker requires Android 4.1+ and OkHttp 3.x.

**Warning**: The data generated and stored when using this interceptor may contain sensitive information such as Authorization or Cookie headers, and the contents of request and response bodies. It is intended for use during development, and not in release builds or other production deployments.

You can redact headers that may contain sensitive information by calling `redactHeader()`.
```java
interceptor.redactHeader("Authorization");
interceptor.redactHeader("Cookie");
```

# Setup

Add the dependency in your `build.gradle` file. Add it alongside the `no-op` variant to isolate Chucker from release builds as follows:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

```gradle
dependencies {
  debugImplementation 'com.github.chuckerteam.chucker:library:2.0.4'
  releaseImplementation 'com.github.chuckerteam.chucker:library-no-op:2.0.4'
}
```

In your application code, create an instance of `ChuckerInterceptor` and its `ChuckerCollector` (you'll need to provide it with a `Context`, because Android) and add it as an interceptor when building your OkHttp client:

```java
// Collector
ChuckerCollector collector = new ChuckerCollector(this)
    .showNotification(true)
    .retentionManager(new RetentionManager(this, ChuckerCollector.Period.ONE_HOUR));

// Interceptor
ChuckerInterceptor chuckerInterceptor = new ChuckerInterceptor(context, collector)
    .maxContentLength(250000L);

OkHttpClient client = new OkHttpClient.Builder()
  .addInterceptor(chuckerInterceptor)
  .build();
```

That's it! Chucker will now record all HTTP interactions made by your OkHttp client. You can optionally disable the notification by calling `showNotification(false)` on the collector object, and launch the Chucker UI directly within your app with the intent from `Chucker.getLaunchIntent()`.

For errors gathering you can directly use the same collector:

```java
// Collector
ChuckerCollector collector = new ChuckerCollector(this)
    .showNotification(true)
    .retentionManager(new RetentionManager(this, ChuckerCollector.Period.ONE_HOUR));

try {
    // Do something risky
} catch (IOException e) {
    collector.onError("Failed to do something risky", e);
}
```

# Snapshots

Development of Chucker happens in the [develop](https://github.com/ChuckerTeam/chucker/tree/develop) branch. You can get `SNAPSHOT` versions directly from Jitpack if needed.

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

```gradle
dependencies {
  debugImplementation 'com.github.chuckerteam.chucker:library:develop-SNAPSHOT'
  releaseImplementation 'com.github.chuckerteam.chucker:library-no-op:develop-SNAPSHOT'
}
```

⚠ Please note that the latest snapshot might be **unstable**. Use it at your own risk :)

If you're looking for the **latest version source code**, you can always find it on the top of the `master` branch.

# FAQ

* Why are some of my request headers missing?
* Why are retries and redirects not being captured discretely?
* Why are my encoded request/response bodies not appearing as plain text?

Please refer to [this section of the OkHttp wiki](https://github.com/square/okhttp/wiki/Interceptors#choosing-between-application-and-network-interceptors). You can choose to use Chucker as either an application or network interceptor, depending on your requirements.

# Acknowledgements

Chucker uses the following open source libraries:

- [OkHttp](https://github.com/square/okhttp) - Copyright Square, Inc.
- [Gson](https://github.com/google/gson) - Copyright Google Inc.
- [Room](https://developer.android.com/topic/libraries/architecture/room) - Copyright Google Inc.

License
-------

    Copyright (C) 2018 Nicola Corti & Olivier Perez.
    Copyright (C) 2017 Jeff Gilfelt.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.