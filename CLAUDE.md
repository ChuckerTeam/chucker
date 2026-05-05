# Chucker - Android HTTP Inspector

Chucker is an in-app HTTP inspector for Android. It intercepts OkHttp traffic, stores it in a Room database, and provides a built-in UI to browse requests/responses. It ships as two variants: `library` (full, for debug) and `library-no-op` (empty stubs, for release).

## Modules

| Module | What it is | When to touch it |
|--------|-----------|-----------------|
| `library` | Full interceptor + UI + database | Adding/changing HTTP inspection features |
| `library-no-op` | Empty stubs matching library's public API | **Must update whenever library's public API changes** |
| `sample` | Demo app exercising all features | Testing changes, adding demo for new features |

## Quick Start

```bash
# Build everything
./gradlew build

# Run tests (library only has tests)
./gradlew :library:test

# Run all quality checks (do this before pushing)
./gradlew lint ktlintCheck detekt apiCheck

# Auto-fix formatting
./gradlew ktlintFormat

# Install sample app to test changes
./gradlew :sample:installDebug

# Install git hooks (trufflehog + cac-validate + yaakhook)
./gradlew installGitHook
```

## How to Add a New Feature

### Adding a new interceptor option (e.g., new config flag)

1. **Add to `ChuckerInterceptor.Builder`** in `library/src/main/kotlin/.../api/ChuckerInterceptor.kt`
   - Add an `internal var` field on the Builder
   - Add a `public fun` builder method that sets it and returns `this`
   - Use it in the private constructor or pass it to processors

2. **Mirror in no-op** in `library-no-op/src/main/kotlin/.../api/ChuckerInterceptor.kt`
   - Add the same builder method signature, but make it a no-op (just return `this`)

3. **Update binary API files** — run `./gradlew apiDump` to regenerate `library/api/library.api` and `library-no-op/api/library-no-op.api`

4. **Add tests** in `library/src/test/kotlin/.../api/ChuckerInterceptorTest.kt`

5. **Demo it** in `sample/.../OkHttpUtils.kt` where the interceptor is built

### Adding a new data field to HTTP transactions

1. **Add field** to `HttpTransaction` entity in `library/.../internal/data/entity/HttpTransaction.kt`
   - Must have `@ColumnInfo(name = "fieldName")` annotation
   - **Increment database version** in `ChuckerDatabase.kt` (currently version 7)
   - Data will be wiped on upgrade (destructive migration is enabled — this is fine for a debug lib)

2. **Populate it** in `RequestProcessor` or `ResponseProcessor` in `library/.../internal/support/`

3. **Display it** in the relevant UI fragment (`TransactionOverviewFragment`, `TransactionPayloadFragment`)

4. **ProGuard**: `HttpTransaction` is already kept in `proguard-rules.pro`, so new fields are safe

### Adding a new UI screen

1. Create Activity extending `BaseChuckerActivity` in `library/.../internal/ui/`
2. Create ViewModel extending `ViewModel` with LiveData from the repository
3. Use ViewBinding (enabled in build config)
4. Add to `AndroidManifest.xml` — use `android:exported="false"` for internal screens
5. All resource names must start with `chucker_` prefix (enforced by Gradle)

### Adding a new public API class

1. Create in `library/.../api/` package with `public` visibility
2. Use Builder pattern if it needs configuration
3. Create matching no-op stub in `library-no-op/.../api/`
4. Run `./gradlew apiDump` to update `.api` tracking files
5. Add tests

## Code Conventions

### Visibility (STRICT — compiler enforced)
```kotlin
// Every public member MUST have explicit visibility modifier
public class MyClass {              // explicit public required
    public fun doThing() { }        // explicit public required
    internal fun helper() { }       // internal for library-internal use
    private val cache = mutableMapOf<String, String>()
}
```
The `-Xexplicit-api=strict` flag means omitting `public`/`internal`/`private` is a **compile error**.

### Patterns used in this codebase
- **Builder pattern** for public configurable classes (ChuckerInterceptor, ChuckerCollector)
- **Repository pattern** for data access (HttpTransactionRepository → Room DAO)
- **MVVM** for UI (ViewModel + LiveData + ViewBinding)
- **Processor pattern** for request/response handling (RequestProcessor, ResponseProcessor)
- **Null Object pattern** for no-op variant (same API, empty bodies)

### Naming
- Resources: `chucker_` prefix (e.g., `chucker_ic_launcher`, `chucker_main_activity`)
- Test methods: backtick sentences (e.g., `` `image response body is available to Chucker` ``)
- Constants: `private companion object { private const val MAX_CONTENT_LENGTH = 250_000L }`

### Testing
- JUnit 5 (Jupiter) — use `@Test`, `@ParameterizedTest`, `@ExtendWith`
- MockK for mocking — `mockk<ChuckerCollector>()`
- Truth for assertions — `assertThat(result).isEqualTo(expected)`
- MockWebServer for HTTP — set up server, enqueue responses, assert intercepted data
- Test utilities in `library/src/test/.../util/` (TestTransactionFactory, ClientFactory, etc.)

## Things That Will Break Your PR

| Check | Command | Common failure |
|-------|---------|---------------|
| **Detekt** | `./gradlew detekt` | Method complexity >16, condition complexity >5, >5 return statements |
| **KtLint** | `./gradlew ktlintCheck` | Formatting issues (fix with `./gradlew ktlintFormat`) |
| **Lint** | `./gradlew lint` | Warnings treated as errors (except RtlEnabled, GradleDependency) |
| **API Check** | `./gradlew apiCheck` | Public API changed without updating `.api` files (fix with `./gradlew apiDump`) |
| **Tests** | `./gradlew :library:test` | Test failures |
| **Pre-commit hooks** | Auto on commit | TruffleHog finds secrets, CAC validation fails |

## Critical Gotchas

1. **Two libraries must stay in sync** — Any public API change in `library` must be mirrored in `library-no-op`. The no-op has empty implementations but identical method signatures. If they diverge, apps using the debug/release split pattern will fail to compile.

2. **Database is destructive** — Room uses `fallbackToDestructiveMigration()`. Schema changes wipe all data. No migration files exist. This is intentional for a debug tool.

3. **ProGuard keeps only HttpTransaction** — Only `HttpTransaction` is in `proguard-rules.pro`. If you add new entities used by Room or Gson, add them too.

4. **`maxIssues: 1` in detekt** — Even 2 detekt violations will fail the build. Fix issues, don't suppress.

5. **Version comes from git** — At build time, version is the current git tag (if tagged) or `branchname-SNAPSHOT`. Shallow clones may not detect tags correctly.

6. **Resource prefix is enforced** — All layout, string, drawable resources must start with `chucker_`. The build will fail otherwise.

## CI/CD Pipeline

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `pre-merge.yaml` | PRs + push to develop | Tests, lint, detekt, ktlint, apiCheck |
| `publish-snapshot.yaml` | Push to develop | Publishes `SNAPSHOT` to Sonatype |
| `publish-release.yaml` | Push git tag | Publishes release to Sonatype staging |
| `close-and-release-repository.yaml` | Manual dispatch | Promotes staging to Maven Central |
| `gradle-wrapper-validation.yml` | PRs + push to develop | Validates Gradle wrapper integrity |

### How to publish a release
1. Update `VERSION_NAME` in `gradle.properties` (e.g., `4.0.0`)
2. Commit & tag: `git tag 4.0.0 && git push origin 4.0.0`
3. `publish-release.yaml` auto-publishes to Sonatype staging
4. Manually trigger `close-and-release-repository.yaml` to push to Maven Central
5. Bump back to next snapshot: `VERSION_NAME=4.1.0-SNAPSHOT`

### Publishing to JFrog Artifactory (Meesho internal)
```bash
./gradlew artifactoryPublish
```
Publishes as `com.meesho.android.chucker:library` / `com.meesho.android.chucker:library-no-op`.

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 1.9.23, Java 17 target |
| **SDK** | minSdk 21, targetSdk 35, compileSdk 35 |
| **AGP** | 8.9.1 |
| **HTTP** | OkHttp 4.9.0 |
| **Database** | Room 2.6.1 |
| **Serialization** | Gson 2.9.0 |
| **Async** | Kotlin Coroutines 1.7.3 + LiveData |
| **UI** | AppCompat + Material 1.2.1 + ViewBinding |
| **Testing** | JUnit 5, MockK 1.10.2, Robolectric 4.4, Truth 1.1 |
