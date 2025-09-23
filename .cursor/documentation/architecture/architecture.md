# Architecture

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



This document describes the high-level architecture of Chucker, covering the system's modular design, core components, data flow patterns, and build structure. It focuses on how the major subsystems interact to provide HTTP inspection capabilities for Android applications.

For specific API usage patterns, see [Core API](#3.2). For detailed HTTP interception implementation, see [HTTP Interception Flow](#3.3). For build system details, see [Build System](#6.1).

## System Overview

Chucker implements a layered architecture designed around HTTP traffic interception, data persistence, and user interface presentation. The system uses a three-module approach to separate full functionality from no-operation stubs, enabling zero-overhead release builds.

```mermaid
graph TB
    subgraph "Application Layer"
        APP["Android Application"]
        OKHTTP["OkHttpClient"]
    end
    
    subgraph "Chucker Core"
        INTERCEPTOR["ChuckerInterceptor"]
        COLLECTOR["ChuckerCollector"] 
        API["Chucker"]
    end
    
    subgraph "Data Layer"
        REPO["HttpTransactionRepository"]
        DAO["HttpTransactionDao"]
        DB["ChuckerDatabase"]
    end
    
    subgraph "Presentation Layer"
        MAIN_UI["MainActivity"]
        TRANS_UI["TransactionActivity"]
        FRAGMENTS["PayloadFragment<br/>OverviewFragment"]
    end
    
    APP --> OKHTTP
    OKHTTP --> INTERCEPTOR
    INTERCEPTOR --> COLLECTOR
    COLLECTOR --> REPO
    REPO --> DAO
    DAO --> DB
    
    API --> MAIN_UI
    MAIN_UI --> TRANS_UI
    TRANS_UI --> FRAGMENTS
    
    REPO --> MAIN_UI
    REPO --> TRANS_UI
```

**Sources:** [README.md:24-51](), [build.gradle:1-118](), [library/build.gradle:1-157]()

## Module Architecture

The system implements a three-module strategy to provide conditional functionality based on build configuration.

```mermaid
graph LR
    subgraph "Build Variants"
        DEBUG["Debug Build"]
        RELEASE["Release Build"]
    end
    
    subgraph "Library Modules"
        FULL["library<br/>Full Implementation"]
        NOOP["library-no-op<br/>Stub Implementation"]
        SAMPLE["sample<br/>Demo Application"]
    end
    
    subgraph "Core Components"
        INTERCEPTOR_FULL["ChuckerInterceptor<br/>(Functional)"]
        INTERCEPTOR_NOOP["ChuckerInterceptor<br/>(No-op)"]
        DATABASE["Room Database"]
        UI_ACTIVITIES["UI Activities"]
    end
    
    DEBUG --> FULL
    RELEASE --> NOOP
    
    FULL --> INTERCEPTOR_FULL
    NOOP --> INTERCEPTOR_NOOP
    
    INTERCEPTOR_FULL --> DATABASE
    INTERCEPTOR_FULL --> UI_ACTIVITIES
    INTERCEPTOR_NOOP -.-> NULL["No Operations"]
    
    SAMPLE --> FULL
    SAMPLE --> NOOP
```

### Module Responsibilities

| Module | Purpose | Key Components |
|--------|---------|----------------|
| `library` | Full HTTP inspection functionality | `ChuckerInterceptor`, `ChuckerCollector`, Room database, UI activities |
| `library-no-op` | Zero-overhead release stubs | Empty implementations of public API classes |
| `sample` | Integration example and testing | Sample HTTP requests, configuration examples |

**Sources:** [library/build.gradle:1-157](), [library-no-op/build.gradle:1-109](), [sample/build.gradle:1-85]()

## Core Component Data Flow

The HTTP inspection process follows a structured data flow from interception through persistence to presentation.

```mermaid
sequenceDiagram
    participant APP as "Android App"
    participant OKHTTP as "OkHttpClient"  
    participant INTERCEPTOR as "ChuckerInterceptor"
    participant COLLECTOR as "ChuckerCollector"
    participant REPO as "HttpTransactionRepository"
    participant DAO as "HttpTransactionDao"
    participant DB as "ChuckerDatabase"
    participant UI as "MainActivity"
    
    APP->>OKHTTP: "HTTP Request"
    OKHTTP->>INTERCEPTOR: "intercept()"
    INTERCEPTOR->>COLLECTOR: "onRequestSent()"
    COLLECTOR->>REPO: "insertTransaction()"
    REPO->>DAO: "@Insert HttpTransaction"
    DAO->>DB: "SQLite INSERT"
    
    INTERCEPTOR->>OKHTTP: "proceed(chain)"
    OKHTTP-->>INTERCEPTOR: "Response"
    INTERCEPTOR->>COLLECTOR: "onResponseReceived()"
    COLLECTOR->>REPO: "updateTransaction()" 
    REPO->>DAO: "@Update HttpTransaction"
    DAO->>DB: "SQLite UPDATE"
    
    UI->>REPO: "getAllTransactions()"
    REPO->>DAO: "@Query SELECT *"
    DAO-->>REPO: "List<HttpTransaction>"
    REPO-->>UI: "LiveData<List<HttpTransaction>>"
```

**Sources:** Based on the overall system diagrams and repository pattern evident in the codebase structure.

## Database Architecture

Chucker uses Room persistence library with a repository pattern for HTTP transaction storage.

```mermaid
erDiagram
    HttpTransaction ||--o{ HttpHeader : contains
    
    HttpTransaction {
        int id PK
        long requestDate
        long responseDate  
        long tookMs
        string protocol
        string method
        string url
        string requestContentType
        long requestContentLength
        string responseCode
        string responseMessage
        string responseContentType
        long responseContentLength
        string error
        string requestBodyEncoded
        string responseBodyEncoded
    }
    
    HttpHeader {
        int id PK
        long transactionId FK
        string name
        string value
    }
```

### Data Access Pattern

```mermaid
graph TB
    subgraph "Repository Layer"
        REPO["HttpTransactionRepository"]
    end
    
    subgraph "DAO Layer"  
        TRANS_DAO["HttpTransactionDao"]
        HEADER_DAO["HttpHeaderDao"]
    end
    
    subgraph "Database"
        ROOM_DB["ChuckerDatabase<br/>@Database"]
        SQLITE["SQLite Storage"]
    end
    
    subgraph "UI Layer"
        VIEWMODEL["TransactionListViewModel"]
        ACTIVITY["MainActivity"]
    end
    
    VIEWMODEL --> REPO
    ACTIVITY --> VIEWMODEL
    
    REPO --> TRANS_DAO
    REPO --> HEADER_DAO
    
    TRANS_DAO --> ROOM_DB
    HEADER_DAO --> ROOM_DB
    ROOM_DB --> SQLITE
```

**Sources:** [library/build.gradle:69-71]() (Room dependencies), database architecture inferred from overall system design

## Build and Publishing Architecture

The build system supports multi-module compilation, quality checks, and artifact publishing to multiple repositories.

```mermaid
graph TB
    subgraph "Source Control"
        DEVELOP["develop branch"]
        MAIN["main branch"] 
        TAG["Tagged Release"]
    end
    
    subgraph "Build Pipeline"
        GRADLE["Gradle Build System"]
        QUALITY["Quality Gates<br/>• ktlint<br/>• detekt<br/>• lint<br/>• tests"]
        COMPILE["Multi-module Compilation"]
    end
    
    subgraph "Artifact Generation"
        AAR_FULL["library.aar"]
        AAR_NOOP["library-no-op.aar"] 
        SOURCES["Source JARs"]
        POM["POM Files"]
    end
    
    subgraph "Publishing"
        JFROG["JFrog Artifactory"]
        SNAPSHOT_REPO["Snapshot Repository"]
        RELEASE_REPO["Release Repository"]
    end
    
    DEVELOP --> GRADLE
    MAIN --> GRADLE
    TAG --> GRADLE
    
    GRADLE --> QUALITY
    QUALITY --> COMPILE
    
    COMPILE --> AAR_FULL
    COMPILE --> AAR_NOOP
    COMPILE --> SOURCES
    COMPILE --> POM
    
    AAR_FULL --> JFROG
    AAR_NOOP --> JFROG
    SOURCES --> JFROG
    POM --> JFROG
    
    JFROG --> SNAPSHOT_REPO
    JFROG --> RELEASE_REPO
    
    DEVELOP -.-> SNAPSHOT_REPO
    TAG -.-> RELEASE_REPO
```

### Gradle Module Configuration

| Configuration | Purpose | Applied To |
|---------------|---------|------------|
| `debugImplementation` | Full Chucker functionality | Development builds |
| `releaseImplementation` | No-op stubs | Production builds |
| `api` | Exposes OkHttp dependency | Both library modules |
| `implementation` | Internal dependencies | Framework libraries |

**Sources:** [build.gradle:1-118](), [library/build.gradle:115-156](), [library-no-op/build.gradle:67-108](), [sample/build.gradle:69-84]()

## Integration Architecture

Chucker integrates with Android applications through the OkHttp interceptor pattern, providing configuration flexibility and conditional behavior.

```mermaid
graph TB
    subgraph "Application Configuration"
        APP_MODULE["App Module build.gradle"]
        DEBUG_CONFIG["debugImplementation"]
        RELEASE_CONFIG["releaseImplementation"]
    end
    
    subgraph "OkHttp Integration"
        CLIENT_BUILDER["OkHttpClient.Builder"]
        ADD_INTERCEPTOR[".addInterceptor()"]
        CHUCKER_INTERCEPTOR["ChuckerInterceptor"]
    end
    
    subgraph "Runtime Behavior"
        DEBUG_RUNTIME["Debug Runtime<br/>• HTTP Capture<br/>• UI Available<br/>• Data Persistence"]
        RELEASE_RUNTIME["Release Runtime<br/>• No Operations<br/>• Zero Overhead"]
    end
    
    subgraph "Configuration Options"
        COLLECTOR_CONFIG["ChuckerCollector<br/>• showNotification<br/>• retentionPeriod"]
        INTERCEPTOR_CONFIG["ChuckerInterceptor.Builder<br/>• maxContentLength<br/>• redactHeaders<br/>• bodyDecoders"]
    end
    
    APP_MODULE --> DEBUG_CONFIG
    APP_MODULE --> RELEASE_CONFIG
    
    DEBUG_CONFIG --> CLIENT_BUILDER
    RELEASE_CONFIG --> CLIENT_BUILDER
    
    CLIENT_BUILDER --> ADD_INTERCEPTOR
    ADD_INTERCEPTOR --> CHUCKER_INTERCEPTOR
    
    CHUCKER_INTERCEPTOR --> DEBUG_RUNTIME
    CHUCKER_INTERCEPTOR --> RELEASE_RUNTIME
    
    DEBUG_RUNTIME --> COLLECTOR_CONFIG
    DEBUG_RUNTIME --> INTERCEPTOR_CONFIG
```

**Sources:** [README.md:38-50](), [README.md:95-128](), [sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt:1-81]()
