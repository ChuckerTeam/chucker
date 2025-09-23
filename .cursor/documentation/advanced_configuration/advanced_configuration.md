# Advanced Configuration

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [CHANGELOG.md](CHANGELOG.md)
- [README.md](README.md)
- [gradle.properties](gradle.properties)

</details>



This page provides comprehensive guidance on advanced configuration options and customization strategies for Chucker. It covers build-time module selection, runtime interceptor configuration, security settings, performance optimization, and advanced features like custom decoders and export functionality.

For basic integration steps, see [Quick Start](#2). For detailed interceptor configuration patterns, see [Interceptor Configuration](#5.1). For practical implementation examples, see [Sample Application](#5.2).

## Configuration Architecture Overview

Chucker's configuration system operates at multiple levels, from build-time module selection to runtime interceptor customization. The architecture separates concerns between build variants, collector behavior, and interceptor processing.

```mermaid
graph TB
    subgraph "Build-Time Configuration"
        DEBUG["Debug Build"]
        RELEASE["Release Build"]
        FULL["library module<br/>Full Implementation"]
        NOOP["library-no-op module<br/>Stub Implementation"]
    end
    
    subgraph "Runtime Configuration"
        COLLECTOR["ChuckerCollector<br/>Data Management"]
        INTERCEPTOR["ChuckerInterceptor<br/>HTTP Processing"]
        BUILDER["ChuckerInterceptor.Builder<br/>Configuration API"]
    end
    
    subgraph "Configuration Categories"
        SECURITY["Security Settings<br/>• redactHeaders<br/>• maxContentLength"]
        BEHAVIOR["Behavior Settings<br/>• showNotification<br/>• retentionPeriod<br/>• alwaysReadResponseBody"]
        FEATURES["Advanced Features<br/>• addBodyDecoder<br/>• createShortcut<br/>• HAR export"]
    end
    
    DEBUG --> FULL
    RELEASE --> NOOP
    FULL --> COLLECTOR
    COLLECTOR --> INTERCEPTOR
    INTERCEPTOR --> BUILDER
    BUILDER --> SECURITY
    BUILDER --> BEHAVIOR
    BUILDER --> FEATURES
    
    classDef build fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef runtime fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    classDef config fill:#f1f8e9,stroke:#689f38,stroke-width:2px
    
    class DEBUG,RELEASE,FULL,NOOP build
    class COLLECTOR,INTERCEPTOR,BUILDER runtime
    class SECURITY,BEHAVIOR,FEATURES config
```

**Sources:** [README.md:34-43](), [README.md:93-128]()

## Build-Time Configuration Strategy

Chucker uses a dual-module approach to eliminate overhead in production builds. The configuration happens entirely at build time through Gradle dependency declarations.

| Build Variant | Module | Behavior | Size Impact |
|---------------|---------|----------|-------------|
| Debug | `library` | Full HTTP inspection with UI | ~400KB |
| Release | `library-no-op` | Empty stub implementations | ~0KB |
| Test | Either variant | Configurable per test needs | Variable |

The module selection ensures zero runtime overhead in production:

```mermaid
flowchart LR
    GRADLE["Gradle Build System"] --> DEBUG_IMPL["debugImplementation<br/>'library'"]
    GRADLE --> RELEASE_IMPL["releaseImplementation<br/>'library-no-op'"]
    
    DEBUG_IMPL --> FULL_FEATURES["ChuckerInterceptor<br/>ChuckerCollector<br/>UI Components<br/>Database Persistence"]
    RELEASE_IMPL --> EMPTY_STUBS["Empty Method Stubs<br/>No UI Components<br/>No Database Access<br/>Minimal Memory Footprint"]
    
    FULL_FEATURES --> DEBUG_BUILD["Debug APK<br/>+HTTP Inspection"]
    EMPTY_STUBS --> RELEASE_BUILD["Release APK<br/>Zero Overhead"]
```

**Sources:** [README.md:36-42](), [gradle.properties:20-23]()

## Collector Configuration

The `ChuckerCollector` manages data persistence and notification behavior. It serves as the primary interface between the interceptor and the underlying data storage system.

### Core Collector Settings

| Setting | Type | Default | Purpose |
|---------|------|---------|---------|
| `showNotification` | `Boolean` | `true` | Controls notification visibility |
| `retentionPeriod` | `RetentionManager.Period` | `ONE_WEEK` | Data cleanup frequency |

### Advanced Collector Configuration

```mermaid
graph TB
    APP["Android Application"] --> COLLECTOR["ChuckerCollector"]
    COLLECTOR --> NOTIFICATION["Notification System"]
    COLLECTOR --> RETENTION["RetentionManager"]
    COLLECTOR --> REPO["HttpTransactionRepository"]
    
    NOTIFICATION --> CHANNEL["Notification Channels<br/>• HTTP Traffic<br/>• Background Operations"]
    RETENTION --> CLEANUP["Periodic Cleanup<br/>• ONE_HOUR<br/>• ONE_DAY<br/>• ONE_WEEK"]
    REPO --> DATABASE["Room Database<br/>HttpTransactionDao"]
    
    subgraph "Configuration Options"
        SHOW_NOTIF["showNotification: Boolean"]
        RETENTION_PERIOD["retentionPeriod: Period"]
    end
    
    SHOW_NOTIF --> NOTIFICATION
    RETENTION_PERIOD --> RETENTION
```

**Sources:** [README.md:96-103](), [CHANGELOG.md:101-102]()

## Interceptor Builder Configuration

The `ChuckerInterceptor.Builder` provides a fluent API for comprehensive interceptor customization. This replaces the deprecated parameterized constructor approach.

### Security and Privacy Settings

| Method | Parameters | Purpose |
|--------|------------|---------|
| `redactHeaders()` | `String...` | Hide sensitive header values |
| `maxContentLength()` | `Long` | Limit body content size |

### Processing Behavior Settings

| Method | Parameters | Purpose |
|--------|------------|---------|
| `alwaysReadResponseBody()` | `Boolean` | Force complete response reading |
| `addBodyDecoder()` | `BodyDecoder` | Custom binary format decoding |
| `createShortcut()` | `Boolean` | Android shortcut generation |

### Advanced Configuration Flow

```mermaid
sequenceDiagram
    participant DEV as "Developer"
    participant BUILDER as "ChuckerInterceptor.Builder"
    participant COLLECTOR as "ChuckerCollector"
    participant INTERCEPTOR as "ChuckerInterceptor"
    participant OKHTTP as "OkHttpClient"
    
    DEV->>BUILDER: "new Builder(context)"
    DEV->>BUILDER: "collector(chuckerCollector)"
    DEV->>BUILDER: "maxContentLength(250_000L)"
    DEV->>BUILDER: "redactHeaders('Auth-Token')"
    DEV->>BUILDER: "alwaysReadResponseBody(true)"
    DEV->>BUILDER: "addBodyDecoder(protoDecoder)"
    DEV->>BUILDER: "createShortcut(true)"
    BUILDER->>INTERCEPTOR: "build()"
    DEV->>OKHTTP: "addInterceptor(interceptor)"
    INTERCEPTOR->>COLLECTOR: "Uses configured collector"
```

**Sources:** [README.md:106-122](), [CHANGELOG.md:61](), [CHANGELOG.md:78-80]()

## Custom Body Decoders

Chucker supports custom decoders for binary formats like Protocol Buffers, Thrift, or proprietary encodings. Decoders are applied in the order they were added.

### Decoder Interface Implementation

The `BinaryDecoder` interface requires two methods:

| Method | Parameters | Returns | Purpose |
|--------|------------|---------|---------|
| `decodeRequest()` | `Request, ByteString` | `String?` | Decode request bodies |
| `decodeResponse()` | `Response, ByteString` | `String?` | Decode response bodies |

### Decoder Configuration Pattern

```mermaid
flowchart TB
    BINARY_DATA["Binary Request/Response"] --> DECODER_CHAIN["Decoder Chain"]
    
    subgraph "Decoder Chain Processing"
        PROTO_DECODER["ProtoDecoder<br/>Protocol Buffers"]
        THRIFT_DECODER["ThriftDecoder<br/>Apache Thrift"] 
        CUSTOM_DECODER["CustomDecoder<br/>Proprietary Format"]
        
        PROTO_DECODER --> THRIFT_DECODER
        THRIFT_DECODER --> CUSTOM_DECODER
    end
    
    DECODER_CHAIN --> DECODED_TEXT["Decoded Text<br/>for UI Display"]
    DECODER_CHAIN --> FALLBACK["Binary Hex Display<br/>if no decoder matches"]
    
    subgraph "Configuration"
        ADD_DECODER["addBodyDecoder(decoder1)"]
        ADD_DECODER2["addBodyDecoder(decoder2)"]
        ADD_DECODER3["addBodyDecoder(decoder3)"]
    end
    
    ADD_DECODER --> PROTO_DECODER
    ADD_DECODER2 --> THRIFT_DECODER  
    ADD_DECODER3 --> CUSTOM_DECODER
```

**Sources:** [README.md:147-164](), [CHANGELOG.md:8-9]()

## Performance and Resource Management

### Content Length Limits

The `maxContentLength` setting prevents memory issues with large response bodies:

| Limit | Use Case | Memory Impact |
|-------|----------|---------------|
| `250_000L` | Default recommendation | Moderate |
| `1_000_000L` | High-throughput APIs | High |
| `50_000L` | Memory-constrained devices | Low |

### Response Body Reading Strategy

The `alwaysReadResponseBody` setting affects how Chucker handles response consumption:

```mermaid
graph TB
    HTTP_RESPONSE["HTTP Response"] --> READ_STRATEGY{"alwaysReadResponseBody"}
    
    READ_STRATEGY -->|"true"| FORCE_READ["Force Complete Read<br/>• Captures parsing errors<br/>• Works with Void/Unit types<br/>• Higher memory usage"]
    READ_STRATEGY -->|"false"| NORMAL_READ["Normal Read Strategy<br/>• Relies on client consumption<br/>• Lower memory usage<br/>• May miss incomplete reads"]
    
    FORCE_READ --> MULTICAST["Multicast Stream<br/>to Client & Chucker"]
    NORMAL_READ --> PASSTHROUGH["Passthrough Stream<br/>with Monitoring"]
    
    MULTICAST --> TEMP_FILE["Temporary File Buffer<br/>Prevents OOM"]
    PASSTHROUGH --> DIRECT_STREAM["Direct Stream Access"]
```

**Sources:** [README.md:113-116](), [CHANGELOG.md:148-149]()

## Export and Integration Features

### HAR Export Configuration

Chucker supports HTTP Archive (HAR) format export for compatibility with external tools:

| Feature | Availability | Use Case |
|---------|-------------|----------|
| Single transaction HAR | Current | Detailed analysis |
| Transaction list HAR | Current | Batch processing |
| Automated HAR export | Future | CI/CD integration |

### Android Shortcuts

The `createShortcut` feature creates dynamic shortcuts for quick Chucker access:

```mermaid
graph LR
    SHORTCUT_CONFIG["createShortcut(true)"] --> DYNAMIC_SHORTCUT["Android Dynamic Shortcut"]
    DYNAMIC_SHORTCUT --> LAUNCHER["App Launcher<br/>Long Press Menu"]
    LAUNCHER --> DIRECT_ACCESS["Direct Chucker UI Access<br/>Bypasses notifications"]
    
    subgraph "Shortcut Lifecycle"
        APP_START["Application Start"] --> INTERCEPTOR_INIT["ChuckerInterceptor Init"]
        INTERCEPTOR_INIT --> SHORTCUT_CREATE["Create Dynamic Shortcut"]
        APP_DESTROY["Application Destroy"] --> SHORTCUT_REMOVE["Remove Dynamic Shortcut"]
    end
```

**Sources:** [CHANGELOG.md:10](), [CHANGELOG.md:12-13](), [README.md:120-121]()

## Migration and Compatibility

### Builder Pattern Migration

The parameterized constructor approach is deprecated in favor of the builder pattern:

| Deprecated | Replacement |
|------------|-------------|
| `ChuckerInterceptor(context, collector, ...)` | `ChuckerInterceptor.Builder(context).collector(...).build()` |

### Version-Specific Features

| Feature | Minimum Version | Status |
|---------|----------------|--------|
| Body decoders | 4.0.0-SNAPSHOT | Development |
| Dynamic shortcuts | 4.0.0-SNAPSHOT | Development |
| HAR export | 3.3.0+ | Stable |
| Builder pattern | 3.4.0+ | Stable |

**Sources:** [CHANGELOG.md:25](), [CHANGELOG.md:61](), [README.md:145]()
