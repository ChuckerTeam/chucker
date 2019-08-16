Change Log
==========

Version 3.0.1 *(2019-08-16)*
----------------------------

This is a hotfix release for Chucker `3.0.0`.

**Summary of Changes**

* Fix: [#96] Limit size of binary image to 1 million bytes.

**Contributors**

This release was possible thanks to the contribution of: @redwarp


Version 3.0.0 *(2019-08-12)*
----------------------------

This is a new major release of Chucker. Please note that this major release contains multiple new features (see below) as well as several breaking changes. Please refer to the [migration guide](/docs/migrating-from-2.0.md) if you need support in migrating from `2.x` -> `3.0.0` or feel free to open an issue.

**Summary of Changes**

* Chucker DB is now using [Room](https://developer.android.com/topic/libraries/architecture/room) instead of [Cupboard](https://bitbucket.org/littlerobots/cupboard/wiki/Home) as ORM.
* The public api of Chucker (classes in `com.chuckerteam.chucker.api`) is now rewritten in Kotlin.
* Classes inside the `.internal` package should now not be considered part of the public api and expect them to change without major version bump.
* Removed usage of `okhttp3.internal` methods.
* General UI update of the library (new using ConstraintLayout)
* Added support to render images in Response page.
* Added support to search and highlight text in the Http Response body.
* We moved the artifact from JCenter to JitPack

**Wall of PRs**

* New: [#67] Add support for images
* New: [#53] Remove usage of methods from okhttp3.internal
* New: [#49] Add missing `@JvmOverload` on the ChuckerInterceptor
* New: [#29] Replacing Cupboard with Room
* New: [#24] Redact header (from okhttp logging)
* New: [#23] Polishing the Color palette
* New: [#22] Modernize the project icon
* New: [#21] Polish the MainActivity
* New: [#17] Highlight search result in response tab
* New: [#10] Updating the ListItem layout to use CL
* New: [#6] Issue #43 Ask for confirmation when delete history
* Breaking: [#66] Renamed registerDefaultCrashHanlder to registerDefaultCrashHandler
* Breaking: [#36] Hide the RetentionManager in the API (Issue #31)
* Breaking: [#42] Kotlinize the Public API of Chucker
* Breaking: [#35] Cleanup form the old Chuck names
* Fix: [#87] Improving counting of seen transactions in notification message
* Fix: [#86] Add positions to string formatting placeholders
* Fix: [#81] Fix for null values being omitted in the parsed body
* Fix: [#75] Fix application name retrieval.
* Fix: [#72] Fetch payload in AsyncTask
* Fix: [#71] Fix crash when clicking notification
* Fix: [#77] Fix image being loaded twice for request and response.
* Fix: [#62] LeakCanary memory leak report
* Fix: [#44] Fix for 'Push Notification is wrongly reporting the first HTTP request
* Fix: [#39] Prevent potential XXE attacks from XML formatting
* Fix: [#38] Do not display URL Query if the query is null
* Fix: [#54] clean up
* Fix: [#50] ChuckerInterceptor Cleanup
* Fix: [#4] Add no-op check to Chuck utility class
* Infra: [#65] Kotlin to 1.3.41
* Infra: [#59] Gradle to 5.5
* Infra: [#51] Setup KtLint and Detekt
* Infra: [#41] Switch Release Artifact to JitPack
* Infra: [#40] Reorganize dependency versions
* Infra: [#28] Updating Dependencies
* Infra: [#16] [TECH] Configure a debug keystore

**Contributors**

This release was possible thanks to the contribution of:

@alorma
@Ashok-Varma
@cortinico
@koral--
@olivierperez
@OlliZi
@PaulWoitaschek
@psh
@redwarp
@uOOOO

Version 2.0.4 *(2019-05-18)*
----------------------------

 * Fix: [#27] no-op Chuck#init method is missing

Version 2.0.3 *(2018-11-28)*
----------------------------

 * Fix: [#20] Changed SqLite DB Version Number 3 -> 4

Version 2.0.2 *(2018-11-14)*
----------------------------

 * Fix: [#5] Empty Content-Encoding are allowed again

Version 2.0.1 *(2018-11-05)*
----------------------------

 * New: Adds a class ChuckCollector that can be used out of ChuckInterceptor.
 * New: Chucker can now collect throwables.
 * New: Adds a notification for throwables.
 * New: Adds screens for throwables.
 * New: Transaction class offers a fluent writing.
 * New: Adds Chuck.registerDefaultCrashHanlder for **debugging purpose only**.
 * Breaking: Chuck.getLaunchIntent needs one more parameter.
 * Breaking: Built with Android plugin 3.1.x.
 * Breaking: Target SDK version 27.
 * Breaking: Support library 27.1.1.
 * Breaking: API classes are now in package `api`.
 * Misc: Move some internal classes.

Version 1.1.0 *(2017-08-06)*
----------------------------

 * Fix: Supports apps targeting Android O (API 26).

Version 1.0.4 *(2017-02-22)*
----------------------------

 * New: Displays uncompressed gzip encoded request/response bodies when used as a network interceptor.

Version 1.0.3 *(2017-02-14)*
----------------------------

 * New: Adds a maximum content length threshold, beyond which bodies are truncated.
 * New: Adds a data retention length property and cleanup task.
 * New: Adds a clear action to the notification.
 * Fix: Mitigates against CursorWindow blowout when transactions are large.

Version 1.0.2 *(2017-02-10)*
----------------------------

 * Fix: Added Proguard rule for compat SearchView.
 * Fix: Null search query displaying invalid results.

Version 1.0.1 *(2017-02-09)*
----------------------------

 * New: Adds a search action which filters on request path or response code.
 * New: Adds a transaction count to the notification.
 * Fix: Limits the size of the static transaction buffer correctly.

Version 1.0.0 *(2017-02-07)*
----------------------------

Initial release.

[#4]: https://github.com/ChuckerTeam/chucker/pull/4
[#5]: https://github.com/ChuckerTeam/chucker/pull/5
[#6]: https://github.com/ChuckerTeam/chucker/pull/6
[#10]: https://github.com/ChuckerTeam/chucker/pull/10
[#16]: https://github.com/ChuckerTeam/chucker/pull/16
[#17]: https://github.com/ChuckerTeam/chucker/pull/17
[#20]: https://github.com/ChuckerTeam/chucker/pull/20
[#21]: https://github.com/ChuckerTeam/chucker/pull/21
[#22]: https://github.com/ChuckerTeam/chucker/pull/22
[#23]: https://github.com/ChuckerTeam/chucker/pull/23
[#24]: https://github.com/ChuckerTeam/chucker/pull/24
[#25]: https://github.com/ChuckerTeam/chucker/pull/25
[#27]: https://github.com/ChuckerTeam/chucker/pull/27
[#28]: https://github.com/ChuckerTeam/chucker/pull/28
[#29]: https://github.com/ChuckerTeam/chucker/pull/29
[#33]: https://github.com/ChuckerTeam/chucker/pull/33
[#34]: https://github.com/ChuckerTeam/chucker/pull/34
[#35]: https://github.com/ChuckerTeam/chucker/pull/35
[#36]: https://github.com/ChuckerTeam/chucker/pull/36
[#38]: https://github.com/ChuckerTeam/chucker/pull/38
[#39]: https://github.com/ChuckerTeam/chucker/pull/39
[#40]: https://github.com/ChuckerTeam/chucker/pull/40
[#41]: https://github.com/ChuckerTeam/chucker/pull/41
[#42]: https://github.com/ChuckerTeam/chucker/pull/42
[#44]: https://github.com/ChuckerTeam/chucker/pull/44
[#47]: https://github.com/ChuckerTeam/chucker/pull/47
[#48]: https://github.com/ChuckerTeam/chucker/pull/48
[#49]: https://github.com/ChuckerTeam/chucker/pull/49
[#50]: https://github.com/ChuckerTeam/chucker/pull/50
[#51]: https://github.com/ChuckerTeam/chucker/pull/51
[#53]: https://github.com/ChuckerTeam/chucker/pull/53
[#54]: https://github.com/ChuckerTeam/chucker/pull/54
[#59]: https://github.com/ChuckerTeam/chucker/pull/59
[#62]: https://github.com/ChuckerTeam/chucker/pull/62
[#63]: https://github.com/ChuckerTeam/chucker/pull/63
[#65]: https://github.com/ChuckerTeam/chucker/pull/65
[#66]: https://github.com/ChuckerTeam/chucker/pull/66
[#67]: https://github.com/ChuckerTeam/chucker/pull/67
[#71]: https://github.com/ChuckerTeam/chucker/pull/71
[#72]: https://github.com/ChuckerTeam/chucker/pull/72
[#75]: https://github.com/ChuckerTeam/chucker/pull/75
[#77]: https://github.com/ChuckerTeam/chucker/pull/77
[#81]: https://github.com/ChuckerTeam/chucker/pull/81
[#86]: https://github.com/ChuckerTeam/chucker/pull/86
[#87]: https://github.com/ChuckerTeam/chucker/pull/87
[#96]: https://github.com/ChuckerTeam/chucker/pull/96
