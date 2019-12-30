# Migrating from Chuck

Please refer to this page if you're **migrating from [chuck](https://github.com/jgilfelt/chuck) to [chucker](https://github.com/ChuckerTeam/chucker)**

The following guide is written assuming you're planning to use Chucker version 3.0.0.

## 1. Update the Gradle dependency

If you're using Chuck you need to update your gradle `dependencies` block from this:

```groovy
dependencies {
    debugCompile "com.readystatesoftware.chuck:library:1.1.0"
    releaseCompile "com.readystatesoftware.chuck:library-no-op:1.1.0"
}
```

to this:

```groovy
repositories {
    // Other repositories here.
    maven { url "https://jitpack.io" }
}

dependencies {
    debugImplementation "com.github.ChuckerTeam.Chucker:library:3.0.1"
    releaseImplementation "com.github.ChuckerTeam.Chucker:library-no-op:3.0.1"
}
```

Please note that Chucker is distributed on **JitPack** at the moment so you need to specify it in the `repositories` block.

## 2. Update the Interceptor initialization code

You need to update the initialization code of Chuck from this:

```kotlin
import com.readystatesoftware.chuck.ChuckInterceptor;

val client = OkHttpClient.Builder()
  .addInterceptor(new ChuckInterceptor(context))
  .build()
```

to this:

```java 
import com.chuckerteam.chucker.api.ChuckerInterceptor;

val client = OkHttpClient.Builder()
  .addInterceptor(new ChuckerInterceptor(context))
  .build()
```

## 3. Update the code to configure the interceptor

Chuck used to use a _Builder_ pattern to configure your interceptor. Chucker instead is using _Kotlin named parameters_ with default values to configure the interceptor. Multiple builder methods have been **removed** and you need to replace them with parameters from the constructors.

Moreover the functions `retainDataFor` and `showNotification` on the interceptor have been **moved** to the `ChuckerCollector` class. Please note that those methods are not available on the Interceptor anymore and you need to update your code accordingly.

### Java

The following code:

```java
ChuckInterceptor interceptor = new ChuckInterceptor(context)
    .showNotification(true)
    .retainDataFor(Period.ONE_HOUR)
    .maxContentLength(120000L);
```

should be updated to:

```java
ChuckerCollector collector = new ChuckerCollector(
    context, true, RetentionManager.Period.ONE_HOUR)

ChuckerInterceptor interceptor = new ChuckerInterceptor(
    context, collector, 120000L);
```

### Kotlin

We suggest to use Kotlin to configure your interceptor as it makes the code more clean/elegant.

The following code:

```kotlin
val interceptor = ChuckInterceptor(context)
    .showNotification(true)
    .retainDataFor(Period.ONE_HOUR)
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

## Naming

Generally name of classes from Chuck (e.g. `ChuckInterceptor`) got udpated to Chucker (e.g. `ChuckerInterceptor`). This is valid for all the classes in the library.

So if to launch the UI of Chuck, you would normally call:

```kotlin
Chuck.getLaunchIntent(...)
```

now you will call

```kotlin
Chucker.getLaunchIntent(...)
```
