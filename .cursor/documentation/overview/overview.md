# Overview

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [CHANGELOG.md](CHANGELOG.md)
- [README.md](README.md)
- [build.gradle](build.gradle)
- [gradle.properties](gradle.properties)
- [gradle/wrapper/gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties)
- [library-no-op/build.gradle](library-no-op/build.gradle)
- [library/build.gradle](library/build.gradle)
- [library/src/main/kotlin/com/chuckerteam/chucker/internal/ui/BaseChuckerActivity.kt](library/src/main/kotlin/com/chuckerteam/chucker/internal/ui/BaseChuckerActivity.kt)
- [sample/build.gradle](sample/build.gradle)
- [sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt](sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt)

</details>



This document provides a comprehensive overview of Chucker, an Android library for inspecting HTTP(S) requests and responses during application development. Chucker functions as an OkHttp interceptor that captures, persists, and displays network traffic through a dedicated user interface.

For detailed integration instructions, see [Quick Start](#2). For advanced configuration options, see [Advanced Configuration](#5). For information about the build system and contributing, see [Development](#6).

## Purpose and Scope

Chucker simplifies HTTP network debugging by providing real-time inspection capabilities for Android applications. The library captures all HTTP traffic processed through OkHttp clients and presents it in an organized, searchable interface. Key characteristics include:

- **Development-focused**: Designed for debug builds with zero overhead in release builds
- **OkHttp integration**: Works as a standard OkHttp interceptor
- **Persistent storage**: Uses Room database for transaction history
- **Rich UI**: Comprehensive viewing and sharing capabilities
- **Multi-module architecture**: Clean separation between full and no-op implementations

Sources: [README.md:24-26](), [README.md:32-67]()

## System Architecture Overview

Chucker employs a multi-layered architecture that separates concerns between network interception, data persistence, and user interface presentation.

### High-Level Component Relationships

```mermaid
graph TB
    subgraph "Android Application"
        APP["OkHttp Client"]
        BUSINESS["Application Logic"]
    end
    
    subgraph "Chucker Library (library module)"
        INTERCEPTOR["ChuckerInterceptor"]
        COLLECTOR["ChuckerCollector"] 
        REPO["HttpTransactionRepository"]
        DAO["HttpTransactionDao"]
        DB[("ChuckerDatabase<br/>(Room)")]
        
        UI_MAIN["MainActivity"]
        UI_TRANS["TransactionActivity"]
        UI_FRAG["PayloadFragment"]
    end
    
    subgraph "No-Op Library (library-no-op module)"
        NOOP_INT["ChuckerInterceptor<br/>(stub)"]
        NOOP_COL["ChuckerCollector<br/>(stub)"]
    end
    
    subgraph "Sample Application"
        SAMPLE["MainActivity"]
        HTTP_TASKS["HttpBinHttpTask<br/>DummyImageHttpTask<br/>PostmanEchoHttpTask"]
    end
    
    APP --> INTERCEPTOR
    INTERCEPTOR --> COLLECTOR
    COLLECTOR --> REPO
    REPO --> DAO
    DAO --> DB
    
    COLLECTOR -.-> UI_MAIN
    UI_MAIN --> UI_TRANS
    UI_TRANS --> UI_FRAG
    
    SAMPLE --> HTTP_TASKS
    HTTP_TASKS --> APP
    
    classDef coreLib fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef ui fill:#f1f8e9,stroke:#689f38,stroke-width:2px
    classDef noop fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef sample fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class INTERCEPTOR,COLLECTOR,REPO,DAO,DB coreLib
    class UI_MAIN,UI_TRANS,UI_FRAG ui
    class NOOP_INT,NOOP_COL noop
    class SAMPLE,HTTP_TASKS sample
```

Sources: [library/build.gradle:58-82](), [library-no-op/build.gradle:43-46](), [sample/build.gradle:69-84]()

### Module Architecture Strategy

```mermaid
graph LR
    subgraph "Build Variants"
        DEBUG["Debug Build"]
        RELEASE["Release Build"]
    end
    
    subgraph "library module"
        FULL_API["Public API"]
        HTTP_INT["ChuckerInterceptor"]
        DATA_LAYER["Room Database<br/>HttpTransactionRepository"]
        UI_LAYER["MainActivity<br/>TransactionActivity"]
        NOTIF["NotificationHelper"]
    end
    
    subgraph "library-no-op module" 
        STUB_API["Public API<br/>(identical interface)"]
        STUB_INT["ChuckerInterceptor<br/>(empty implementation)"]
    end
    
    subgraph "Consumer Application"
        CONSUMER["OkHttpClient.Builder()"]
    end
    
    DEBUG -->|debugImplementation| FULL_API
    RELEASE -->|releaseImplementation| STUB_API
    
    CONSUMER --> DEBUG
    CONSUMER --> RELEASE
    
    FULL_API --> HTTP_INT
    FULL_API --> DATA_LAYER
    FULL_API --> UI_LAYER
    FULL_API --> NOTIF
    
    STUB_API --> STUB_INT
    
    classDef debug fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
    classDef release fill:#ffecb3,stroke:#f57c00,stroke-width:2px
    classDef consumer fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    
    class DEBUG,FULL_API,HTTP_INT,DATA_LAYER,UI_LAYER,NOTIF debug
    class RELEASE,STUB_API,STUB_INT release
    class CONSUMER consumer
```

Sources: [README.md:36-43](), [library/build.gradle:1-56](), [library-no-op/build.gradle:1-46]()

## Core Components

### Network Interception Layer

The `ChuckerInterceptor` serves as the primary entry point for HTTP traffic capture. It integrates with OkHttp's interceptor chain to observe requests and responses without disrupting normal network operations.

| Component | Location | Responsibility |
|-----------|----------|----------------|
| `ChuckerInterceptor` | `library/src/main/kotlin/com/chuckerteam/chucker/api/ChuckerInterceptor.kt` | OkHttp interceptor implementation |
| `ChuckerCollector` | `library/src/main/kotlin/com/chuckerteam/chucker/api/ChuckerCollector.kt` | Data collection and processing |
| `BodyDecoder` | `library/src/main/kotlin/com/chuckerteam/chucker/api/BodyDecoder.kt` | Custom body decoding interface |

Sources: [README.md:45-51](), [README.md:93-128]()

### Data Persistence Layer

Chucker uses Room database for persistent storage of HTTP transactions, enabling historical analysis and offline viewing.

```mermaid
graph TB
    subgraph "Data Layer Architecture"
        PUBLIC_API["Chucker object<br/>(Public API)"]
        REPO["HttpTransactionRepository"]
        DAO["HttpTransactionDao"]
        DB[("ChuckerDatabase")]
        ENTITY["HttpTransaction<br/>(Room Entity)")]
        
        PUBLIC_API --> REPO
        REPO --> DAO
        DAO --> DB
        DB --> ENTITY
    end
    
    subgraph "Configuration"
        RETENTION["RetentionManager"]
        PERIOD["RetentionManager.Period<br/>ONE_HOUR, ONE_DAY, etc."]
        
        RETENTION --> PERIOD
    end
    
    REPO --> RETENTION
    
    classDef data fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef config fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    
    class PUBLIC_API,REPO,DAO,DB,ENTITY data
    class RETENTION,PERIOD config
```

Sources: [library/build.gradle:69-71](), [README.md:97-103]()

### User Interface Layer

The UI system provides comprehensive transaction viewing and management capabilities through multiple interconnected activities and fragments.

```mermaid
graph TB
    subgraph "UI Components"
        BASE["BaseChuckerActivity"]
        MAIN["MainActivity<br/>(Transaction List)"]
        TRANS["TransactionActivity<br/>(Transaction Details)"]
        
        OVERVIEW["TransactionOverviewFragment"]
        REQUEST["TransactionRequestFragment"] 
        RESPONSE["TransactionResponseFragment"]
        PAYLOAD["TransactionPayloadFragment"]
    end
    
    subgraph "UI Features"
        SEARCH["Search & Filter"]
        EXPORT["Export (cURL, HAR)"]
        SHARE["Share Functionality"]
        NOTIF["Notification Integration"]
    end
    
    BASE --> MAIN
    BASE --> TRANS
    MAIN --> TRANS
    TRANS --> OVERVIEW
    TRANS --> REQUEST
    TRANS --> RESPONSE
    TRANS --> PAYLOAD
    
    MAIN --> SEARCH
    TRANS --> EXPORT
    TRANS --> SHARE
    MAIN --> NOTIF
    
    classDef baseUi fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef activities fill:#f1f8e9,stroke:#689f38,stroke-width:2px  
    classDef fragments fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    classDef features fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class BASE baseUi
    class MAIN,TRANS activities
    class OVERVIEW,REQUEST,RESPONSE,PAYLOAD fragments
    class SEARCH,EXPORT,SHARE,NOTIF features
```

Sources: [library/src/main/kotlin/com/chuckerteam/chucker/internal/ui/BaseChuckerActivity.kt:15-44](), [README.md:81-89]()

## Runtime Data Flow

The following sequence illustrates how HTTP transactions flow through Chucker's components during runtime execution:

```mermaid
sequenceDiagram
    participant App as "Android Application"
    participant OkHttp as "OkHttpClient"
    participant Interceptor as "ChuckerInterceptor"
    participant Collector as "ChuckerCollector"
    participant Repository as "HttpTransactionRepository"
    participant Database as "ChuckerDatabase"
    participant UI as "MainActivity"
    
    Note over App,UI: HTTP Request Lifecycle
    
    App->>OkHttp: "makeRequest()"
    OkHttp->>Interceptor: "intercept(Chain)"
    Interceptor->>Collector: "onRequestSent()"
    Collector->>Repository: "insertTransaction()"
    Repository->>Database: "INSERT HttpTransaction"
    
    Interceptor->>OkHttp: "chain.proceed(request)"
    OkHttp-->>App: "HTTP Response"
    
    OkHttp->>Interceptor: "response received"
    Interceptor->>Collector: "onResponseReceived()"
    Collector->>Repository: "updateTransaction()"
    Repository->>Database: "UPDATE HttpTransaction"
    
    Note over App,UI: UI Interaction
    
    Collector-->>UI: "showNotification() (optional)"
    UI->>Repository: "getAllTransactions()"
    Repository->>Database: "SELECT * FROM transactions"
    Database-->>UI: "List<HttpTransaction>"
```

Sources: [README.md:147-164](), [sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt:76-79]()

## Build System Integration

Chucker's build system supports automated artifact publishing and quality assurance through multiple CI/CD workflows.

| Build Component | Purpose | Configuration |
|----------------|---------|---------------|
| Gradle Modules | Multi-module structure | [build.gradle:1-118]() |
| Artifact Publishing | Maven Central distribution | [library/build.gradle:115-156]() |
| Quality Gates | Code quality enforcement | [build.gradle:94-98]() |
| Version Management | Semantic versioning | [gradle.properties:20-23]() |

The system employs a dual-dependency strategy where debug builds include full functionality while release builds use no-op stubs, ensuring zero runtime overhead in production applications.

Sources: [build.gradle:64-99](), [library/build.gradle:115-156](), [library-no-op/build.gradle:67-108]()
