Change Log
==========

Version 3.1.0 *(2020-01-24)*
----------------------------

This is a new minor release of Chucker. Please note that this minor release contains multiple new features (see below) as well as multiple bugfixes. 

**Summary of Changes**

* The library is now fully converted to Kotlin!
* The whole UI has been revamped to support Dark Theme.
* The library is now using `ViewModel` internally to handle state changes.
* The Response/Request Body is now displayed in a `RecyclerView`, drastically improving performances on big payloads.
* HTTP Response/Request Body can now be saved on file.
* Notifications for Throwable and HTTP Traffic are now going on two separate channels.
* A lot of classes inside the `.internal` package have restricted visibility (from public to internal).

**Wall of PRs**

* [#182] New: Notifications update
* [#160] New: Add date formatting for requestDate and responseDate
* [#156] New: Update assets to match changed design
* [#145] New: Introduce a shared view model into transaction details activity + fragments
* [#143] New: Feature/dark theme
* [#138] New: Add an option to save the request or response body
* [#119] New: Display explanation text on tabs if empty
* [#118] New: Update the request/response body to use a monospace font
* [#98]  New: Add search button on request tab
* [#198] Fix: Clone the Response Buffer rather than using it directly
* [#188] Fix: Hide library resources
* [#186] Fix: Fix for wrong class used for extending in ErrorActivity
* [#181] Fix: Mask true type of headersToRedact and update documentation
* [#180] Fix: Visibility fix for internal members
* [#172] Fix: Fixing handling of big body payloads
* [#171] Fix: Fix "null" prefix in POM name
* [#167] Fix: Fix issue with LinearLayoutManager after code minification
* [#165] Fix: Close the native source.
* [#148] Fix: Another fix for notifications in Q
* [#147] Fix: Remove unused variable in ClearDatabaseService
* [#146] Fix: Pass the -module-name Kotlin Compiler flag
* [#144] Fix: Fix notification importance for Android Q
* [#121] Fix: Force all layouts to be LTR
* [#103] Fix: Fix content type for response formatting
* [#85]  Fix: Fix race conditions in NotificationHelper.
* [#190] Refactor: ChuckerInterceptor refactoring
* [#189] Refactor: Headers redaction refactoring
* [#169] Refactor: Minor code cleanup
* [#149] Refactor: Update/share util
* [#142] Refactor: Cleanup kotlin classes
* [#139] Refactor: Remove deprecations
* [#137] Refactor: Unification for activities start
* [#136] Refactor: Kotlinify classes in .internal.ui.transaction package
* [#135] Refactor: Kotlinify classes in .internal.ui package
* [#126] Refactor: Fix inconsistency when referencing screen constants from Java
* [#125] Refactor: Kotlinify Chucker sample app
* [#123] Refactor: Kotlinify classes in .internal.ui.error package
* [#122] Refactor: Kotlinify NotificationHelper
* [#120] Refactor: Kotlinify FormatUtils
* [#114] Refactor: Kotlinify IOUtils
* [#112] Refactor: Kotlinify ClearDatabaseService
* [#111] Refactor: Removing SimpleOnPageChangedListener
* [#109] Refactor: Kotlinify ChuckerCrashHandler
* [#108] Refactor: Kotlinify JsonConverter
* [#101] Refactor: Move the .internal package outside of .api
* [#193] Infra: Update Multiple Dependencies
* [#185] Infra: CI pipeline optimisation
* [#176] Infra: Bump KtLint to 0.36.0
* [#175] Infra: Remove redundant publishing tools
* [#174] Infra: Remove stale support-lib-version from gradle file
* [#162] Infra: Update/components versions
* [#133] Infra: Updating Detekt to 1.1.0
* [#107] Infra: Detekt to 1.0.1
* [#104] Infra: Kotlin to 1.3.50

**Contributors**

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
