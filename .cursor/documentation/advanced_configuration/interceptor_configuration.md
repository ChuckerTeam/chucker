# Interceptor Configuration

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [CHANGELOG.md](CHANGELOG.md)
- [README.md](README.md)
- [gradle.properties](gradle.properties)

</details>



This document covers the detailed configuration options for `ChuckerInterceptor` and `ChuckerCollector`, including security settings, custom decoders, and advanced behavioral controls. For information about the overall HTTP interception architecture and flow, see [HTTP Interception Flow](#3.3). For practical integration examples, see [Sample Application](#5.2).

## Configuration Architecture

The Chucker interceptor configuration system is built around two main components: `ChuckerCollector` for data management and `ChuckerInterceptor.Builder` for request/response processing configuration.

```mermaid
graph TB
    subgraph "Configuration Components"
        CC["ChuckerCollector"]
        CIB["ChuckerInterceptor.Builder"]
        CI["ChuckerInterceptor"]
        OHC["OkHttpClient"]
    end
    
    subgraph "Collector Configuration"
        SN["showNotification: Boolean"]
        RP["retentionPeriod: Period"]
        CTX["context: Context"]
    end
    
    subgraph "Interceptor Configuration"
        MCL["maxContentLength: Long"]
        RH["redactHeaders: List<String>"]
        ARB["alwaysReadResponseBody: Boolean"]
        BD["addBodyDecoder: BinaryDecoder"]
        CS["createShortcut: Boolean"]
    end
    
    subgraph "Runtime Behavior"
        NM["Notification Management"]
        DM["Data Management"]
        HP["Header Processing"]
        BP["Body Processing"]
        DC["Data Collection"]
    end
    
    CTX --> CC
    SN --> CC
    RP --> CC
    
    CC --> CIB
    MCL --> CIB
    RH --> CIB
    ARB --> CIB
    BD --> CIB
    CS --> CIB
    
    CIB --> CI
    CI --> OHC
    
    CC --> NM
    CC --> DM
    RH --> HP
    BD --> BP
    MCL --> BP
    ARB --> DC
    
    classDef config fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef runtime fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    
    class CC,CIB,CI config
    class NM,DM,HP,BP,DC runtime
```

**Sources:** [README.md:93-128]()

## ChuckerCollector Configuration

`ChuckerCollector` manages data persistence, retention policies, and notification behavior. It serves as the data management layer for the interceptor system.

### Core Configuration Options

| Option | Type | Purpose | Default |
|--------|------|---------|---------|
| `context` | `Context` | Android application context for database and notifications | Required |
| `showNotification` | `Boolean` | Controls visibility of HTTP activity notifications | `true` |
| `retentionPeriod` | `RetentionManager.Period` | Data retention policy for collected transactions | Varies |

### Data Retention Policies

The `retentionPeriod` parameter accepts predefined periods from `RetentionManager.Period`:
- `ONE_HOUR` - Transactions older than 1 hour are automatically purged
- `ONE_DAY` - 24-hour retention window
- `ONE_WEEK` - 7-day retention window

```mermaid
graph LR
    subgraph "ChuckerCollector Lifecycle"
        INIT["ChuckerCollector(context, showNotification, retentionPeriod)"]
        NTF["Notification System"]
        DB["HttpTransactionRepository"]
        RM["RetentionManager"]
    end
    
    INIT --> NTF
    INIT --> DB
    INIT --> RM
    
    subgraph "Runtime Operations"
        CT["Collect Transaction"]
        SN["Show Notification"]
        PD["Persist Data"]
        CD["Clean Data"]
    end
    
    NTF --> SN
    DB --> PD
    RM --> CD
    CT --> SN
    CT --> PD
```

**Sources:** [README.md:96-103]()

## ChuckerInterceptor Builder Configuration

The `ChuckerInterceptor.Builder` provides a fluent API for configuring request and response processing behavior. This builder pattern was introduced to maintain binary compatibility and provide extensible configuration options.

### Request/Response Processing Configuration

```mermaid
flowchart TD
    START["ChuckerInterceptor.Builder(context)"]
    
    START --> COLLECTOR["collector(ChuckerCollector)"]
    START --> MAXCONTENT["maxContentLength(Long)"]
    START --> REDACT["redactHeaders(vararg String)"]
    START --> ALWAYS["alwaysReadResponseBody(Boolean)"]
    START --> DECODER["addBodyDecoder(BinaryDecoder)"]
    START --> SHORTCUT["createShortcut(Boolean)"]
    
    COLLECTOR --> BUILD["build()"]
    MAXCONTENT --> BUILD
    REDACT --> BUILD
    ALWAYS --> BUILD
    DECODER --> BUILD
    SHORTCUT --> BUILD
    
    BUILD --> INTERCEPTOR["ChuckerInterceptor"]
    
    subgraph "Configuration Effects"
        CE1["Data Collection Strategy"]
        CE2["Content Processing Rules"]
        CE3["Security Controls"]
        CE4["UI Integration"]
    end
    
    COLLECTOR --> CE1
    MAXCONTENT --> CE2
    ALWAYS --> CE2
    DECODER --> CE2
    REDACT --> CE3
    SHORTCUT --> CE4
```

**Sources:** [README.md:105-122](), [CHANGELOG.md:61-62]()

### Configuration Parameter Details

| Method | Parameter Type | Purpose |
|--------|----------------|---------|
| `collector()` | `ChuckerCollector` | Associates data collection and persistence behavior |
| `maxContentLength()` | `Long` | Sets byte limit for response body processing (truncation threshold) |
| `redactHeaders()` | `vararg String` | Specifies headers to mask with `**` in UI |
| `alwaysReadResponseBody()` | `Boolean` | Forces complete response consumption even on parsing errors |
| `addBodyDecoder()` | `BinaryDecoder` | Adds custom decoder for binary formats (Protobuf, Thrift, etc.) |
| `createShortcut()` | `Boolean` | Controls Android dynamic shortcut creation |

## Security Configuration

Security configuration focuses on preventing sensitive data exposure in the Chucker UI through header redaction and content filtering.

### Header Redaction

Headers containing sensitive information can be automatically redacted in the Chucker UI. The `redactHeaders()` method accepts multiple header names and replaces their values with `**` masking.

```mermaid
graph TB
    subgraph "Header Processing Pipeline"
        REQ["HTTP Request"]
        HE["Header Extraction"]
        RC["Redaction Check"]
        MASK["Apply Masking"]
        STORE["Store in Database"]
        UI["Display in UI"]
    end
    
    subgraph "Redaction Configuration"
        RH["redactHeaders('Auth-Token', 'Bearer', 'Cookie')"]
        RL["Redaction List"]
    end
    
    REQ --> HE
    HE --> RC
    RH --> RL
    RL --> RC
    
    RC -->|"Header in redaction list"| MASK
    RC -->|"Header not in list"| STORE
    MASK --> STORE
    STORE --> UI
    
    subgraph "UI Display"
        AUTH["Auth-Token: **"]
        BEARER["Bearer: **"]
        CONTENT["Content-Type: application/json"]
    end
    
    UI --> AUTH
    UI --> BEARER
    UI --> CONTENT
```

**Sources:** [README.md:111-112](), [README.md:136-141]()

### Content Length Limitations

The `maxContentLength` setting provides protection against memory exhaustion by truncating response bodies that exceed the specified byte limit.

**Sources:** [README.md:109-110]()

## Custom Body Decoders

Custom body decoders enable Chucker to display binary content formats that are not natively supported (plain text, Gzip, Brotli).

### BinaryDecoder Interface

Custom decoders must implement the `BinaryDecoder` interface, providing separate methods for request and response processing:

```mermaid
classDiagram
    class BinaryDecoder {
        +decodeRequest(request: Request, body: ByteString) String?
        +decodeResponse(response: Response, body: ByteString) String?
    }
    
    class ProtoDecoder {
        +decodeRequest(request: Request, body: ByteString) String?
        +decodeResponse(response: Response, body: ByteString) String?
        -decodeProtoBody(body: ByteString) String
        -isExpectedProtoRequest(request: Request) Boolean
        -isExpectedProtoResponse(response: Response) Boolean
    }
    
    class ThriftDecoder {
        +decodeRequest(request: Request, body: ByteString) String?
        +decodeResponse(response: Response, body: ByteString) String?
    }
    
    BinaryDecoder <|-- ProtoDecoder
    BinaryDecoder <|-- ThriftDecoder
    
    class ChuckerInterceptorBuilder {
        +addBodyDecoder(decoder: BinaryDecoder) Builder
    }
    
    ChuckerInterceptorBuilder --> BinaryDecoder : uses
```

### Decoder Processing Chain

Multiple decoders can be installed and are applied in the order they were added. Each decoder can return `null` to indicate it cannot process the content, allowing the next decoder in the chain to attempt processing.

```mermaid
sequenceDiagram
    participant CI as "ChuckerInterceptor"
    participant D1 as "ProtoDecoder"
    participant D2 as "ThriftDecoder"
    participant D3 as "DefaultDecoder"
    
    Note over CI,D3: Response Body Processing
    CI->>D1: decodeResponse(response, body)
    alt Proto Format Detected
        D1-->>CI: Decoded String
    else Not Proto Format
        D1-->>CI: null
        CI->>D2: decodeResponse(response, body)
        alt Thrift Format Detected
            D2-->>CI: Decoded String
        else Not Thrift Format
            D2-->>CI: null
            CI->>D3: decodeResponse(response, body)
            D3-->>CI: Raw/Default Processing
        end
    end
```

**Sources:** [README.md:118-119](), [README.md:149-164](), [CHANGELOG.md:8-10]()

## OkHttpClient Integration

The final configuration step involves integrating the configured `ChuckerInterceptor` with the `OkHttpClient`. The interceptor can be added as either an application or network interceptor, each providing different visibility into the HTTP processing pipeline.

```mermaid
graph LR
    subgraph "OkHttpClient Configuration"
        BUILDER["OkHttpClient.Builder()"]
        AI["addInterceptor(chuckerInterceptor)"]
        NI["addNetworkInterceptor(chuckerInterceptor)"]
        BUILD["build()"]
    end
    
    subgraph "Interceptor Types"
        APP["Application Interceptor<br/>- Sees redirect/retry requests<br/>- Sees original headers<br/>- May not see network failures"]
        NET["Network Interceptor<br/>- Sees final network request<br/>- Sees compressed responses<br/>- Sees network-level headers"]
    end
    
    BUILDER --> AI
    BUILDER --> NI
    AI --> BUILD
    NI --> BUILD
    
    AI -.-> APP
    NI -.-> NET
    
    subgraph "HTTP Pipeline"
        REQ["Request"]
        APP_LAYER["Application Interceptors"]
        CACHE["Cache"]
        NET_LAYER["Network Interceptors"]  
        NETWORK["Network"]
    end
    
    REQ --> APP_LAYER
    APP_LAYER --> CACHE
    CACHE --> NET_LAYER
    NET_LAYER --> NETWORK
```

**Sources:** [README.md:124-127](), [README.md:206-210]()
