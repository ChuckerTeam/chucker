# Change Log

## Version 3.1.2 *(2020-02-09)*

This is hot-fix release to fix multiple issues introduced in `3.1.0`.

### Summary of Changes
* All Chucker screens now have their own `ViewModel`. Due to this change user can now open the transaction in progress and the content will appear as soon as transaction finishes. No need for reopening transaction anymore.

### Bugfixes

* Fixed an [issue](https://github.com/ChuckerTeam/chucker/issues/225) introduced in 3.1.0 where image downloading fails if OkHttp was used for image loading in libraries like Glide, Picasso or Coil.
* Fixed an [issue](https://github.com/ChuckerTeam/chucker/pull/214) with invalid CURL command generation.
* Fixed an [issue](https://github.com/ChuckerTeam/chucker/issues/217) with crashes if ProGuard/R8 minification is applied to Chucker.
* Fixed an [issue](https://github.com/ChuckerTeam/chucker/pull/221) with crash when user taps Save in a transaction, which is still in progress.
* Fixed an [issue](https://github.com/ChuckerTeam/chucker/pull/222) with crash when user taps Clear from notification shade while the original app is already dead.
* Fixed an [issue](https://github.com/ChuckerTeam/chucker/pull/223) with possible NPEs.

### Credits

This release was possible thanks to the contribution of:

@MiSikora
@vbuberen

## Version 3.1.1 *(2020-01-25)*

This is hot-fix release to fix issue introduced in `3.1.0`.

### Summary of Changes

- Fixed an [issue](https://github.com/ChuckerTeam/chucker/issues/203) introduced in 3.1.0 where some of response bodies were shown as `null` and their sizes were 0 bytes.

### Credits

This release was possible thanks to the contribution of:

@cortinico

## Version 3.1.0 *(2020-01-24)*

### This version shouldn't be used as dependency due to [#203](https://github.com/ChuckerTeam/chucker/issues/203). Use 3.1.1 instead.

This is a new minor release of Chucker. Please note that this minor release contains multiple new features (see below) as well as multiple bugfixes. 

### Summary of Changes

* The library is now fully converted to Kotlin and migrated to AndroidX!
* The whole UI has been revamped to support Dark Theme which follows your device theme.
* The Response/Request Body is now displayed in a `RecyclerView`, drastically improving performances on big payloads.
* HTTP Response/Request Body can now be saved in file.
* Notifications for Throwable and HTTP Traffic are now going into separate channels.
* A lot of classes inside the `.internal` package have restricted visibility (from public to internal). Also, resources like strings, dimens and drawables from Chucker won't appear in your autocomplete suggestions.

### Bugfixes

* Fixed ANRs during big response payloads processing.
* Fixed contentType response formatting.
* Fixed notifications importance in Android Q.
* Fixed date formatting in transaction overview.
* Fixed visibility of internal library classes and resources.
* Fixed XML formatting crash

### Dependency Updates

- Updated Kotlin to 1.3.61
- Updated Retrofit to 2.6.4
- Updated Room to 2.2.3
- Updated OkHttp to 3.12.6
- Updated Gson to 2.8.6
- Updated Dokka to 0.10.0
- Updated KtLint to 9.1.1
- Updated Gradle wrapper to 6.1
- Updated Android Gradle plugin to 3.5.3

#### Credits

This release was possible thanks to the contribution of:

@christopherniksch
@yoavst 
@psh
@kmayoral
@vbuberen
@dcampogiani 
@ullas-jain
@rakshit444
@olivierperez
@p-schneider
@Volfor
@cortinico
@koral--
@redwarp
@uOOOO
@sprohaszka 
@PaulWoitaschek 


## Version 3.0.1 *(2019-08-16)*

This is a hotfix release for Chucker `3.0.0`.

### Summary of Changes

* Fix: [#96] Limit size of binary image to 1 million bytes.

### Credits

This release was possible thanks to the contribution of: @redwarp


## Version 3.0.0 *(2019-08-12)*

This is a new major release of Chucker. Please note that this major release contains multiple new features (see below) as well as several breaking changes. Please refer to the [migration guide](/docs/migrating-from-2.0.md) if you need support in migrating from `2.x` -> `3.0.0` or feel free to open an issue.

### Summary of Changes

* Chucker DB is now using [Room](https://developer.android.com/topic/libraries/architecture/room) instead of [Cupboard](https://bitbucket.org/littlerobots/cupboard/wiki/Home) as ORM.
* The public api of Chucker (classes in `com.chuckerteam.chucker.api`) is now rewritten in Kotlin.
* Classes inside the `.internal` package should now not be considered part of the public api and expect them to change without major version bump.
* Removed usage of `okhttp3.internal` methods.
* General UI update of the library (new using ConstraintLayout)
* Added support to render images in Response page.
* Added support to search and highlight text in the Http Response body.
* We moved the artifact from JCenter to JitPack

### Contributors

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

## Version 2.0.4 *(2019-05-18)*

 * Fix: [#27] no-op Chuck#init method is missing

## Version 2.0.3 *(2018-11-28)*

 * Fix: [#20] Changed SqLite DB Version Number 3 -> 4

## Version 2.0.2 *(2018-11-14)*

 * Fix: [#5] Empty Content-Encoding are allowed again

## Version 2.0.1 *(2018-11-05)*

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

## Version 1.1.0 *(2017-08-06)*

 * Fix: Supports apps targeting Android O (API 26).

## Version 1.0.4 *(2017-02-22)*

 * New: Displays uncompressed gzip encoded request/response bodies when used as a network interceptor.

## Version 1.0.3 *(2017-02-14)*

 * New: Adds a maximum content length threshold, beyond which bodies are truncated.
 * New: Adds a data retention length property and cleanup task.
 * New: Adds a clear action to the notification.
 * Fix: Mitigates against CursorWindow blowout when transactions are large.

## Version 1.0.2 *(2017-02-10)*

 * Fix: Added Proguard rule for compat SearchView.
 * Fix: Null search query displaying invalid results.

## Version 1.0.1 *(2017-02-09)*

 * New: Adds a search action which filters on request path or response code.
 * New: Adds a transaction count to the notification.
 * Fix: Limits the size of the static transaction buffer correctly.

## Version 1.0.0 *(2017-02-07)*

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
[#85]: https://github.com/ChuckerTeam/chucker/pull/85
[#90]: https://github.com/ChuckerTeam/chucker/pull/90
[#98]: https://github.com/ChuckerTeam/chucker/pull/98
[#99]: https://github.com/ChuckerTeam/chucker/pull/99
[#101]: https://github.com/ChuckerTeam/chucker/pull/101
[#103]: https://github.com/ChuckerTeam/chucker/pull/103
[#104]: https://github.com/ChuckerTeam/chucker/pull/104
[#107]: https://github.com/ChuckerTeam/chucker/pull/107
[#108]: https://github.com/ChuckerTeam/chucker/pull/108
[#109]: https://github.com/ChuckerTeam/chucker/pull/109
[#111]: https://github.com/ChuckerTeam/chucker/pull/111
[#112]: https://github.com/ChuckerTeam/chucker/pull/112
[#114]: https://github.com/ChuckerTeam/chucker/pull/114
[#118]: https://github.com/ChuckerTeam/chucker/pull/118
[#119]: https://github.com/ChuckerTeam/chucker/pull/119
[#120]: https://github.com/ChuckerTeam/chucker/pull/120
[#121]: https://github.com/ChuckerTeam/chucker/pull/121
[#122]: https://github.com/ChuckerTeam/chucker/pull/122
[#123]: https://github.com/ChuckerTeam/chucker/pull/123
[#125]: https://github.com/ChuckerTeam/chucker/pull/125
[#126]: https://github.com/ChuckerTeam/chucker/pull/126
[#127]: https://github.com/ChuckerTeam/chucker/pull/127
[#130]: https://github.com/ChuckerTeam/chucker/pull/130
[#131]: https://github.com/ChuckerTeam/chucker/pull/131
[#132]: https://github.com/ChuckerTeam/chucker/pull/132
[#133]: https://github.com/ChuckerTeam/chucker/pull/133
[#135]: https://github.com/ChuckerTeam/chucker/pull/135
[#136]: https://github.com/ChuckerTeam/chucker/pull/136
[#137]: https://github.com/ChuckerTeam/chucker/pull/137
[#138]: https://github.com/ChuckerTeam/chucker/pull/138
[#139]: https://github.com/ChuckerTeam/chucker/pull/139
[#142]: https://github.com/ChuckerTeam/chucker/pull/142
[#143]: https://github.com/ChuckerTeam/chucker/pull/143
[#144]: https://github.com/ChuckerTeam/chucker/pull/144
[#145]: https://github.com/ChuckerTeam/chucker/pull/145
[#146]: https://github.com/ChuckerTeam/chucker/pull/146
[#147]: https://github.com/ChuckerTeam/chucker/pull/147
[#148]: https://github.com/ChuckerTeam/chucker/pull/148
[#149]: https://github.com/ChuckerTeam/chucker/pull/149
[#150]: https://github.com/ChuckerTeam/chucker/pull/150
[#153]: https://github.com/ChuckerTeam/chucker/pull/153
[#156]: https://github.com/ChuckerTeam/chucker/pull/156
[#160]: https://github.com/ChuckerTeam/chucker/pull/160
[#162]: https://github.com/ChuckerTeam/chucker/pull/162
[#165]: https://github.com/ChuckerTeam/chucker/pull/165
[#167]: https://github.com/ChuckerTeam/chucker/pull/167
[#169]: https://github.com/ChuckerTeam/chucker/pull/169
[#170]: https://github.com/ChuckerTeam/chucker/pull/170
[#171]: https://github.com/ChuckerTeam/chucker/pull/171
[#172]: https://github.com/ChuckerTeam/chucker/pull/172
[#173]: https://github.com/ChuckerTeam/chucker/pull/173
[#174]: https://github.com/ChuckerTeam/chucker/pull/174
[#175]: https://github.com/ChuckerTeam/chucker/pull/175
[#176]: https://github.com/ChuckerTeam/chucker/pull/176
[#180]: https://github.com/ChuckerTeam/chucker/pull/180
[#181]: https://github.com/ChuckerTeam/chucker/pull/181
[#182]: https://github.com/ChuckerTeam/chucker/pull/182
[#183]: https://github.com/ChuckerTeam/chucker/pull/183
[#185]: https://github.com/ChuckerTeam/chucker/pull/185
[#186]: https://github.com/ChuckerTeam/chucker/pull/186
[#188]: https://github.com/ChuckerTeam/chucker/pull/188
[#189]: https://github.com/ChuckerTeam/chucker/pull/189
[#190]: https://github.com/ChuckerTeam/chucker/pull/190
[#191]: https://github.com/ChuckerTeam/chucker/pull/191
[#193]: https://github.com/ChuckerTeam/chucker/pull/193
[#196]: https://github.com/ChuckerTeam/chucker/pull/196
[#198]: https://github.com/ChuckerTeam/chucker/pull/198
[#201]: https://github.com/ChuckerTeam/chucker/pull/201
