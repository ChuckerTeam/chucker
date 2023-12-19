# ComposeChucker
[![Maven Central](https://img.shields.io/maven-central/v/com.github.chuckerteam.chucker/library)](https://search.maven.org/artifact/com.github.chuckerteam.chucker/library) ![Pre Merge Checks](https://github.com/ChuckerTeam/chucker/workflows/Pre%20Merge%20Checks/badge.svg?branch=develop)  ![License](https://img.shields.io/github/license/ChuckerTeam/Chucker.svg) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-orange.svg)](http://makeapullrequest.com)

_A fork of [Chucker](https://github.com/ChuckerTeam/chucker)_

<p align="center">
  <img src="assets/ic_launcher-web.png" alt="ComposeChucker icon" width="30%"/>
</p>

- [ComposeChucker](#compose-chucker)
  - [Getting Started 👣](#getting-started-)
  - [Features 🧰](#features-)
    - [Multi-Window 🚪](#multi-window-)
  - [Configure 🎨](#configure-)
    - [Redact-Header 👮‍♂️](#redact-header-️)
    - [Decode-Body 📖](#decode-body-)
    - [Notification Permission 🔔](#notification-permission-)
  - [Migrating 🚗](#migrating-)
  - [Snapshots 📦](#snapshots-)
  - [FAQ ❓](#faq-)
- [Sponsors 💸](#sponsors-)
  - [Contributing 🤝](#contributing-)
    - [Building 🛠](#building-)
  - [Acknowledgments 🌸](#acknowledgments-)
    - [Maintainers](#maintainers)
    - [Thanks](#thanks)
    - [Libraries](#libraries)
  - [License 📄](#license-)

ComposeChucker is fork of Chucker in Jetpack Compose which simplifies the inspection of **HTTP(S) requests/responses** fired by your Compose Android App. ComposeChucker works as an **OkHttp Interceptor** persisting all those events inside your application, and providing a UI for inspecting and sharing their content.

Apps using ComposeChucker will display a **notification** showing a summary of ongoing HTTP activity. Tapping on the notification launches the full ComposeChucker UI. Apps can optionally suppress the notification, and launch the ComposeChucker UI directly from within their own interface.

<p align="center">
  <img src="assets/chucker-http.gif" alt="ComposeChucker http sample" width="50%"/>
</p>

## Getting Started 👣

ComposeChucker is distributed through [Maven Central](https://search.maven.org/artifact/com.github.chuckerteam.chucker/library). To use it you need to add the following **Gradle dependency** to the `build.gradle` file of your android app module (NOT the root file).

Please note that you should add both the `library` and the `library-no-op` variant to isolate ComposeChucker from release builds as follows:

```groovy
dependencies {
  debugImplementation "com.github.chuckerteam.chucker:library:4.0.0"
  releaseImplementation "com.github.chuckerteam.chucker:library-no-op:4.0.0"
}
```

To start using ComposeChucker, just plug in a new `ComposeChuckerInterceptor` to your OkHttp Client Builder:

```kotlin
val client = OkHttpClient.Builder()
                .addInterceptor(ComposeChuckerInterceptor(context))
                .build()
```

**That's it!** 🎉 ComposeChucker will now record all HTTP interactions made by your OkHttp client.

## Features 🧰

Don't forget to check the [changelog](CHANGELOG.md) to have a look at all the changes in the latest version of ComposeChucker.

* Compatible with **OkHTTP 4**
* **API >= 21** compatible
* Easy to integrate (just 2 gradle `implementation` lines).
* Works **out of the box**, no customization needed.
* **Empty release artifact** 🧼 (no traces of ComposeChucker in your final APK).
* Support for body text search with **highlighting** 🕵️‍♂️
* Support for showing **images** in HTTP Responses 🖼
* Support for custom decoding of HTTP bodies

### Multi-Window 🚪

The main ComposeChucker activity is launched in its own task, allowing it to be displayed alongside the host app UI using Android 7.x multi-window support.

![Multi-Window](assets/chucker-multiwindow.gif)

## Configure 🎨

You can customize ComposeChucker providing an instance of a `ComposeChuckerCollector`:

```kotlin
// Create the Collector
val composeChuckerCollector = ComposeComposeChuckerCollector(
        context = this,
        // Toggles visibility of the notification
        showNotification = true,
        // Allows to customize the retention period of collected data
        retentionPeriod = RetentionManager.Period.ONE_HOUR
)

// Create the Interceptor
val composeChuckerInterceptor = ComposeChuckerInterceptor.Builder(context)
        // The previously created Collector
        .collector(composeChuckerCollector)
        // The max body content length in bytes, after this responses will be truncated.
        .maxContentLength(250_000L)
        // List of headers to replace with ** in the ComposeChucker UI
        .redactHeaders("Auth-Token", "Bearer")
        // Read the whole response body even when the client does not consume the response completely.
        // This is useful in case of parsing errors or when the response body
        // is closed before being read like in Retrofit with Void and Unit types.
        .alwaysReadResponseBody(true)
        // Use decoder when processing request and response bodies. When multiple decoders are installed they
        // are applied in an order they were added.
        .addBodyDecoder(decoder)
        // Controls Android shortcut creation.
        .createShortcut(true)
        .build()

// Don't forget to plug the ComposeChuckerInterceptor inside the OkHttpClient
val client = OkHttpClient.Builder()
        .addInterceptor(composeChuckerInterceptor)
        .build()
```

### Redact-Header 👮‍♂️

**Warning** The data generated and stored when using ComposeChucker may contain sensitive information such as Authorization or Cookie headers, and the contents of request and response bodies.

It is intended for **use during development**, and not in release builds or other production deployments.

You can redact headers that contain sensitive information by calling `redactHeader(String)` on the `ComposeChuckerInterceptor`.


```kotlin
interceptor.redactHeader("Auth-Token", "User-Session");
```

### Decode-Body 📖

ComposeChucker by default handles only plain text, Gzip compressed or Brotli compressed. If you use a binary format like, for example, Protobuf or Thrift it won't be automatically handled by ComposeChucker. You can, however, install a custom decoder that is capable of reading data from different encodings.

```kotlin
object ProtoDecoder : BodyDecoder {
    fun decodeRequest(request: Request, body: ByteString): String? = if (request.isExpectedProtoRequest) {
        decodeProtoBody(body)
    } else {
        null
    }

    fun decodeResponse(request: Response, body: ByteString): String? = if (request.isExpectedProtoResponse) {
        decodeProtoBody(body)
    } else {
        null
    }
}
interceptorBuilder.addBodyDecoder(ProtoDecoder).build()
```

### Notification Permission 🔔

Starting with Android 13, your apps needs to request the `android.permission.POST_NOTIFICATIONS` permission to the user in order to show notifications.
As ComposeChucker also shows notifications to show network activity you need to handle permission request depending on your app features.
Without this permission ComposeChucker will track network activity, but there will be no notifications on devices with Android 13 and newer.

There are 2 possible cases:
1. If your app is already sending notifications, you don't need to do anything as ComposeChucker will
show a notification as soon as the `android.permission.POST_NOTIFICATIONS` permission is granted to your app.
1. If your app does not send notifications you would need to open ComposeChucker directly (can be done via shortcut, which is added to your app by default when ComposeChucker is added)
and click `Allow` in the dialog with permission request. In case you don't allow this permission or dismiss that dialog by mistake, on every ComposeChucker launch there will be
a snackbar with a button to open your app settings where you can change permissions settings. Note, you need to grant `android.permission.POST_NOTIFICATIONS` to your app in Settings as there
will be no separate app in Apps list in Settings.

## Snapshots 📦

Development of ComposeChucker happens in the [`main`](https://github.com/ChuckerTeam/chucker/tree/dev) branch. Every push to `main` will trigger a publishing of a `SNAPSHOT` artifact for the upcoming version. You can get those snapshots artifacts directly from Sonatype with:

```gradle
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}
dependencies {
  debugImplementation "com.github.chuckerteam.chucker:library:4.1.0-SNAPSHOT"
  releaseImplementation "com.github.chuckerteam.chucker:library-no-op:4.1.0-SNAPSHOT"
}
```


⚠️ Please note that the latest snapshot might be **unstable**. Use it at your own risk ⚠️

If you're looking for the **latest stable version**, you can always find it in `Releases` section.

## FAQ ❓

* Why are some of my request headers (e.g. `Content-Encoding` or `Accept-Encoding`) missing?
* Why are retries and redirects not being captured discretely?
* Why are my encoded request/response bodies not appearing as plain text?

Please refer to [this section of the OkHttp documentation](https://square.github.io/okhttp/interceptors/). You can choose to use ComposeChucker as either an application or network interceptor, depending on your requirements.

# Sponsors 💸

ComposeChucker is maintained and improved during nights, weekends and whenever team has free time. If you use ComposeChucker in your project, please consider sponsoring us. This will help us buy a domain for a website we will have soon and also spend some money on charity. Additionally, sponsorship will also help us understand better how valuable ComposeChucker is for people's everyday work.

You can sponsor us by clicking `Sponsor` button.

## Contributing 🤝

We're offering support for ComposeChucker on the [#chucker](https://kotlinlang.slack.com/archives/CRWD6370R) channel on [kotlinlang.slack.com](https://kotlinlang.slack.com/). Come and join the conversation over there.

**We're looking for contributors! Don't be shy.** 😁 Feel free to open issues/pull requests to help us improve this project.

* When reporting a new Issue, make sure to attach **Screenshots**, **Videos** or **GIFs** of the problem you are reporting.
* When submitting a new PR, make sure tests are all green. Write new tests if necessary.

Short `TODO` List for new contributors:

- Increment the test coverage.
- [Issues marked as `Help wanted`](https://github.com/ChuckerTeam/chucker/labels/help%20wanted)

### Building 🛠

In order to start working on ComposeChucker, you need to fork the project and open it in Android Studio/IntelliJ IDEA.

Before committing we suggest you install the pre-commit hooks with the following command:

```
./gradlew installGitHook
```

This will make sure your code is validated against KtLint and Detekt before every commit.
The command will run automatically before the `clean` task, so you should have the pre-commit hook installed by then.

Before submitting a PR please run:

```
./gradlew build
```

This will build the library and will run all the verification tasks (ktlint, detekt, lint, unit tests) locally.
This will make sure your CI checks will pass.

## Acknowledgments 🌸

### Libraries

ComposeChucker uses the following open source libraries:

- [OkHttp](https://github.com/square/okhttp) - Copyright Square, Inc.
- [Gson](https://github.com/google/gson) - Copyright Google Inc.
- [Room](https://developer.android.com/topic/libraries/architecture/room) - Copyright Google Inc.

## License 📄

```
    Copyright (C) 2018-2021 ComposeChucker Team.
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
```
