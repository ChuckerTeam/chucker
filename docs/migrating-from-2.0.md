# Migrating from Chucker 2.x to 3.x

Please refer to this page if you're **migrating from chucker version `2.0.4` to `3.x`**.

In this page you will find the summary of all the breaking changes that you potentially need to fix.

## 1. Class name changes

Generally name of classes from Chucker 2.x used to have `Chuck` as a prefix (e.g. `ChuckInterceptor`). In version 3.x we updated the naming of all the classes to have `Chucker` as a prefix (e.g. `ChuckerInterceptor`). This is valid for all the classes in the library.

So if to launch the UI of Chucker, you would normally call:

```kotlin
Chuck.getLaunchIntent(...)
```

now you will call

```kotlin
Chucker.getLaunchIntent(...)
```

## 2. Package name changes

Please note that with version 3.x package name is also updated. The new package for the classes of Chucker will be `com.chuckerteam.chucker.api`.

Here a summary of the name/package changes in chucker

| Old | New |
| --- | --- |
| `com.readystatesoftware.chuck.api.Chuck` | `com.chuckerteam.chucker.api.Chucker` |
| `com.readystatesoftware.chuck.api.ChuckCollector` | `com.chuckerteam.chucker.api.ChuckerCollector` |
| `com.readystatesoftware.chuck.api.ChuckerInterceptor` | `com.chuckerteam.chucker.api.ChuckerInterceptor` |

## 3. Update the code to configure the interceptor

Chucker v2.0 used to use a _Builder_ pattern to configure your interceptor. Chucker v3.0 instead is using _Kotlin named parameters_ with default values to configure the interceptor. Multiple builder methods have been **removed** and you need to replace them with parameters from the constructors.

### Java

The following code:

```java
ChuckInterceptor interceptor = new ChuckInterceptor(context, collector)
    .maxContentLength(120000L);
```

should be updated to:

```java
ChuckInterceptor interceptor = new ChuckInterceptor(context, collector, 120000)
```

### Kotlin

We suggest to use Kotlin to configure your interceptor as it makes the code more clean/elegant.

The following code:

```kotlin
val retentionManager = RetentionManager(androidApplication, ChuckCollector.Period.ONE_HOUR)

val collector = ChuckCollector(androidApplication)
            .retentionManager(retentionManager)
            .showNotification(true)

val interceptor = ChuckInterceptor(context, collector)
    .maxContentLength(120000L)
```

should be updated to:

```kotlin
val collector = ChuckerCollector(
    context = this,
    showNotification = true,
    retentionPeriod = RetentionManager.Period.ONE_HOUR
)

val interceptor = ChuckerInterceptor(
    context = context,
    collector = collector,
    maxContentLength = 120000L
)
```

## 4. RetentionManager is now replaced by the retentionPeriod

You don't need to create a `RetentionManager` anymore and you simply have to specify the `retentionPeriod` parameter when creating a `ChuckerCollector`.

The `Period` enum has also been moved from `ChuckCollector` to `RetentionManager`.

## 5. registerDefaultCrashHanlder typo

The function `Chuck.registerDefaultCrashHanlder` contained a typo in the name and now is moved to `Chucker.registerDefaultCrashHandler`.
