# library - Chucker Core Library

The main module. Contains the OkHttp interceptor, Room database, and Android UI for HTTP inspection.

## Architecture

```
api/                    # Public API — what app developers use
  ChuckerInterceptor    #   OkHttp interceptor (Builder pattern)
  ChuckerCollector      #   Data collection lifecycle
  Chucker               #   Utility singleton (launch intent, notifications)
  RetentionManager      #   Data cleanup policy
  BodyDecoder           #   Interface for custom decoders (e.g., Protobuf)

internal/data/          # Persistence layer
  entity/               #   HttpTransaction (Room entity, 60+ fields)
  room/                 #   ChuckerDatabase (v7, destructive migration)
  repository/           #   HttpTransactionRepository (LiveData queries)
  har/                  #   HAR 1.2 export format classes

internal/support/       # Processing & utilities
  RequestProcessor      #   Extracts request metadata + payload
  ResponseProcessor     #   Extracts response metadata + payload (multicast via TeeSource)
  NotificationHelper    #   Shows persistent notification with transaction count
  *Sharable classes     #   Export as text/curl/HAR file
  Stream utilities      #   TeeSource, LimitingSource, DepletingSource, ReportingSink

internal/ui/            # Android MVVM UI
  MainActivity          #   Transaction list (singleTask, separate task affinity)
  TransactionActivity   #   Detail view: Overview + Request + Response tabs
  *ViewModel classes    #   LiveData from repository, filtering by code/path
```

## How to Build & Test

```bash
./gradlew :library:test              # Run all tests (JUnit 5 + Robolectric)
./gradlew :library:lint              # Lint (warnings = errors)
./gradlew :library:assembleDebug     # Build AAR
```

## Data Flow

```
App makes HTTP call via OkHttp
  → ChuckerInterceptor.intercept()
    → RequestProcessor extracts headers, URL, body (with size limit + custom decoders)
    → ChuckerCollector.onRequestSent() → Room DB insert
    → chain.proceed(request) [actual network call]
    → ResponseProcessor extracts status, headers, body (TeeSource for multicast)
    → ChuckerCollector.onResponseReceived() → Room DB update + notification
  → UI observes LiveData from Room → RecyclerView updates automatically
```

## How to Make Changes

### Adding a new field to display in the transaction detail screen

1. If it's a new data field, add to `HttpTransaction` entity with `@ColumnInfo`
2. Increment DB version in `ChuckerDatabase` (currently 7) — data wipes on upgrade, that's fine
3. Populate in `RequestProcessor` or `ResponseProcessor`
4. Add UI in the relevant fragment:
   - `TransactionOverviewFragment` — metadata (URL, status, timing)
   - `TransactionPayloadFragment` — headers and body content
5. If it's a list field, add adapter item type in `TransactionPayloadAdapter`

### Adding a new export format

1. Create class implementing `Sharable` interface in `internal/support/`
2. Add menu option in `TransactionActivity` menu
3. Handle in `TransactionActivity.onOptionsItemSelected()`

### Adding a new public API option

1. Add Builder field + method in `ChuckerInterceptor` (return `this` for chaining)
2. Pass to processor via constructor
3. **Must mirror in library-no-op** — same method signature, empty body
4. Run `./gradlew apiDump` to update `.api` files
5. Add test in `ChuckerInterceptorTest`

### Modifying the Room database

- Entity: `HttpTransaction` in `internal/data/entity/`
- DAO: `HttpTransactionDao` in `internal/data/room/`
- Database: `ChuckerDatabase` — uses `fallbackToDestructiveMigration()` (no migration files needed)
- All fields need `@ColumnInfo` annotation
- `HttpTransaction` is kept by ProGuard (`proguard-rules.pro`) — new fields are safe

## Testing Patterns

```kotlin
// Typical interceptor test structure
@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorTest {
    @get:Rule val server = MockWebServer()

    @Test
    fun `descriptive test name in backticks`() {
        // Use ClientFactory to create OkHttp client with Chucker
        // Use ChuckerInterceptorDelegate to wrap and assert
        // Use TestTransactionFactory for mock data
        // Assert with Truth: assertThat(result).isEqualTo(expected)
    }
}
```

Key test utilities (in `src/test/.../util/`):
- `TestTransactionFactory` — creates mock HttpTransaction objects
- `ClientFactory` — OkHttp client setup variants
- `ChuckerInterceptorDelegate` — interceptor wrapper for assertions
- `NoLoggerRule` — suppresses Chucker logging in tests

## Gotchas

- **Explicit API is strict** — every `public`/`internal`/`private` must be written out. Omitting it = compile error.
- **Resource prefix** — all resources must start with `chucker_`. Build fails otherwise.
- **Detekt limits** — max 16 method complexity, max 5 condition complexity, max 5 returns. `maxIssues: 1`.
- **`MainScope()` in ChuckerCollector is never cancelled** — create one collector and reuse it.
- **Manifest: MainActivity uses `singleTask` + separate task affinity** — this enables multi-window but may conflict with host app's task setup.
- **No RTL support** — lint check is disabled, UI is LTR only.