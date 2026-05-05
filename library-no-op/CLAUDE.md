# library-no-op - Release Build Stubs

Zero-overhead stubs for production/release builds. Same public API as `library`, but every method is empty or passes through. Apps use this pattern:

```gradle
debugImplementation project(':library')           // Full Chucker in debug
releaseImplementation project(':library-no-op')   // Empty stubs in release
```

## What's Here

Five files in `src/main/kotlin/.../api/`, each mirroring the main library's public API:

| Class | What the no-op does |
|-------|-------------------|
| `ChuckerInterceptor` | `intercept()` just calls `chain.proceed(request)` — passthrough. Builder methods return `this`. |
| `Chucker` | `isOp = false`. `getLaunchIntent()` returns empty `Intent()`. |
| `ChuckerCollector` | Constructor accepts same params, stores nothing. |
| `RetentionManager` | `doMaintenance()` is empty. |
| `BodyDecoder` | Interface only (same as main library). |

**Dependencies:** Only OkHttp + Kotlin stdlib. No Room, no AndroidX, no Material, no Coroutines.

## When You Need to Touch This

**Every time you change the public API in `library`:**

1. Add/remove/modify the same method signature here
2. Implementation should be empty (return `this`, return empty value, or do nothing)
3. Run `./gradlew apiDump` to update `api/library-no-op.api`
4. Run `./gradlew apiCheck` to verify compatibility

### Example: Adding a new Builder method

In `library/`:
```kotlin
public fun myNewOption(value: Boolean): Builder = apply {
    this.myNewOption = value
}
```

In `library-no-op/`:
```kotlin
@Suppress("UnusedPrivateMember")
public fun myNewOption(value: Boolean): Builder = this
```

## Gotchas

- **API files may show type erasure differences** — the no-op `.api` file may show `Object` where the main library shows specific types. This is a known quirk of the binary compatibility validator with the no-op's simplified generics.
- **Don't add dependencies** — the whole point is zero overhead. No Room, no UI, no nothing.
- **Same namespace** — both modules use `com.chuckerteam.chucker` package. They're mutually exclusive at build time (debug vs release).
- **No tests** — stubs are trivial. If a stub is complex enough to need tests, it's doing too much.