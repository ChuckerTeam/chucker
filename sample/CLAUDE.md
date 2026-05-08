# sample - Demo Application

Interactive Android app that exercises all Chucker features. Use it to test your changes visually.

## How to Run

```bash
./gradlew :sample:installDebug    # Full Chucker UI (debug)
./gradlew :sample:installRelease  # No-op Chucker (release, to verify no-op works)
```

## What the App Does

Two buttons:
- **"Do HTTP activity"** — Fires 20+ HTTP requests covering every scenario, then open Chucker notification to inspect them
- **"Launch Chucker directly"** — Opens the Chucker UI (debug only)

Radio buttons switch between Application interceptor and Network interceptor modes.

### HTTP scenarios covered

| Task Class | What it tests |
|-----------|--------------|
| `HttpBinHttpTask` | GET, POST, PUT, PATCH, DELETE, redirects, auth, gzip/brotli/deflate, status codes (201/401/500), streaming |
| `DummyImageHttpTask` | Image response bodies (PNG downloads) |
| `PostmanEchoHttpTask` | Large JSON payloads, Protocol Buffer encoding/decoding |

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | UI entry point, triggers HTTP tasks |
| `OkHttpUtils.kt` | **Builds the OkHttpClient with ChuckerInterceptor** — this is the reference for how to integrate Chucker |
| `HttpBinHttpTask.kt` | Most comprehensive HTTP test (20+ varied requests) |
| `PokemonProtoBodyDecoder.kt` | Custom `BodyDecoder` example for Protocol Buffers |
| `LargeJson.kt` | Pre-built large JSON payload for testing truncation |
| `InterceptorType.kt` | Enum + provider for switching Application/Network interceptor |

## How to Use for Testing Your Changes

1. Make your change in `library/`
2. Run `./gradlew :sample:installDebug`
3. Tap "Do HTTP activity" — this fires diverse requests through Chucker
4. Open Chucker from the notification or "Launch Chucker directly" button
5. Verify your change works in the UI

### Adding a demo for a new feature

1. If it's a new interceptor option: configure it in `OkHttpUtils.kt` where the interceptor is built
2. If it needs new HTTP requests: create a new `HttpTask` implementation or add to `HttpBinHttpTask`
3. If it needs a UI trigger: add a button in `activity_main_sample.xml` and wire it in `MainActivity`

## Build Variants

- **Debug**: Full Chucker library, StrictMode enabled, LeakCanary for memory leak detection, cleartext HTTP allowed (network security config)
- **Release**: No-op library, no debug tools

## Gotchas

- **Uses external APIs** (httpbin.org, dummyimage.com, postman-echo.com) — tests fail if these services are down. This is a network-dependent demo, not a unit test.
- **ProGuard is disabled** (`minifyEnabled false`) — this doesn't test minification. If you need to verify ProGuard rules, test with a separate app that has minification enabled.
- **Wire plugin** generates Protobuf classes at build time — if the build seems slow or fails on proto files, check `sample/src/main/proto/`.