# Core API

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt](library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt)
- [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionDatabaseRepository.kt](library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionDatabaseRepository.kt)
- [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionRepository.kt](library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionRepository.kt)
- [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/room/HttpTransactionDao.kt](library/src/main/kotlin/com/chuckerteam/chucker/internal/data/room/HttpTransactionDao.kt)

</details>



This document covers Chucker's main public API and data management layer. It focuses on the `Chucker` object that provides the primary interface for interacting with the library, and the repository pattern used for managing HTTP transaction data persistence.

For information about the interceptor configuration and HTTP capture mechanisms, see [Interceptor Configuration](#5.1). For details about the user interface components, see [Main Interface](#4.1).

## Public API Overview

The core public API is exposed through the `Chucker` object, which serves as the main entry point for developers to interact with the library. This singleton object provides methods for launching the UI, managing notifications, clearing data, and generating HAR exports.

### Chucker Object Structure

```mermaid
graph TB
    subgraph "Public API Layer"
        ChuckerObj["Chucker"]
        ChuckerObj --> getLaunchIntent["getLaunchIntent()"]
        ChuckerObj --> dismissNotifications["dismissNotifications()"]
        ChuckerObj --> clearTransactions["clearTransactions()"]
        ChuckerObj --> generateHar["generateHar()"]
        ChuckerObj --> isOp["isOp: Boolean"]
    end

    subgraph "Internal Support Layer"
        ChuckerObj --> createShortcut["createShortcut()"]
        ChuckerObj --> logger["logger: Logger"]
    end

    subgraph "Dependencies"
        MainActivity["MainActivity"]
        NotificationHelper["NotificationHelper"]
        RepositoryProvider["RepositoryProvider"]
        HarUtils["HarUtils"]
    end

    getLaunchIntent --> MainActivity
    dismissNotifications --> NotificationHelper
    clearTransactions --> RepositoryProvider
    generateHar --> RepositoryProvider
    generateHar --> HarUtils
```

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt:24-116]()

### Key Public Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getLaunchIntent(context)` | Creates Intent to launch Chucker UI | `Intent` |
| `dismissNotifications(context)` | Dismisses all Chucker notifications | `void` |
| `clearTransactions()` | Removes all stored HTTP transactions | `suspend fun` |
| `generateHar(context, limit)` | Exports transactions as HAR format | `suspend fun: ByteArray` |
| `isOp` | Indicates if this is operational instance | `Boolean` |

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt:36-99]()

## Data Management Layer

The data management layer implements the repository pattern to abstract data persistence operations. It consists of three primary components: the repository interface, database implementation, and Room DAO.

### Repository Pattern Architecture

```mermaid
graph TB
    subgraph "API Layer"
        ChuckerAPI["Chucker Object"]
    end

    subgraph "Repository Layer"
        RepoProvider["RepositoryProvider"]
        RepoInterface["HttpTransactionRepository"]
        RepoImpl["HttpTransactionDatabaseRepository"]
    end

    subgraph "Data Access Layer"
        ChuckerDB["ChuckerDatabase"]
        TransactionDao["HttpTransactionDao"]
    end

    subgraph "Entities"
        HttpTransaction["HttpTransaction"]
        HttpTransactionTuple["HttpTransactionTuple"]
    end

    ChuckerAPI --> RepoProvider
    RepoProvider --> RepoInterface
    RepoInterface <|-- RepoImpl
    RepoImpl --> ChuckerDB
    ChuckerDB --> TransactionDao
    TransactionDao --> HttpTransaction
    TransactionDao --> HttpTransactionTuple
```

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionRepository.kt:7-31](), [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionDatabaseRepository.kt:9-47]()

### Repository Interface Operations

The `HttpTransactionRepository` interface defines the contract for data operations:

| Operation Category | Methods |
|-------------------|---------|
| **Create/Update** | `insertTransaction()`, `updateTransaction()` |
| **Delete** | `deleteAllTransactions()`, `deleteOldTransactions()` |
| **Query** | `getSortedTransactionTuples()`, `getFilteredTransactionTuples()`, `getTransaction()` |
| **Bulk Operations** | `getAllTransactions()`, `getLastTransactions()` |

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionRepository.kt:14-31]()

### Database Access Layer

The Room DAO provides SQL-based data access with LiveData support for reactive UI updates:

```mermaid
graph LR
    subgraph "Query Types"
        Sorted["getSortedTuples()"]
        Filtered["getFilteredTuples()"]
        ById["getById()"]
        All["getAll()"]
        LastN["getLastN()"]
    end

    subgraph "Mutation Types"
        Insert["insert()"]
        Update["update()"]
        DeleteAll["deleteAll()"]
        DeleteBefore["deleteBefore()"]
    end

    subgraph "Return Types"
        LiveDataList["LiveData<List<HttpTransactionTuple>>"]
        LiveDataSingle["LiveData<HttpTransaction?>"]
        SuspendList["suspend: List<HttpTransaction>"]
        SuspendUnit["suspend: Unit/Int"]
    end

    Sorted --> LiveDataList
    Filtered --> LiveDataList
    ById --> LiveDataSingle
    All --> SuspendList
    LastN --> SuspendList

    Insert --> SuspendUnit
    Update --> SuspendUnit
    DeleteAll --> SuspendUnit
    DeleteBefore --> SuspendUnit
```

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/room/HttpTransactionDao.kt:15-49]()

## API Operations and Data Flow

The following sequence demonstrates how the Core API components interact during typical operations:

### Transaction Storage and Retrieval Flow

```mermaid
sequenceDiagram
    participant Client as "Client Code"
    participant ChuckerAPI as "Chucker"
    participant RepoProvider as "RepositoryProvider"
    participant Repository as "HttpTransactionRepository"
    participant DAO as "HttpTransactionDao"
    participant DB as "Room Database"

    Note over Client,DB: Data Storage Flow
    Client->>ChuckerAPI: clearTransactions()
    ChuckerAPI->>RepoProvider: transaction()
    RepoProvider->>Repository: deleteAllTransactions()
    Repository->>DAO: deleteAll()
    DAO->>DB: SQL DELETE

    Note over Client,DB: Data Export Flow  
    Client->>ChuckerAPI: generateHar(context, 1000)
    ChuckerAPI->>RepoProvider: transaction()
    RepoProvider->>Repository: getLastTransactions(1000)
    Repository->>DAO: getLastN(1000)
    DAO->>DB: SQL SELECT
    DB-->>DAO: List<HttpTransaction>
    DAO-->>Repository: List<HttpTransaction>
    Repository-->>ChuckerAPI: List<HttpTransaction>
    ChuckerAPI->>ChuckerAPI: HarUtils.harStringFromTransactions()
    ChuckerAPI-->>Client: ByteArray (HAR)

    Note over Client,DB: UI Data Access Flow
    Client->>ChuckerAPI: getLaunchIntent(context)
    ChuckerAPI-->>Client: Intent(MainActivity)
    Note over Client: MainActivity launched
    Note over Repository,DAO: MainActivity queries via Repository
    Repository->>DAO: getSortedTuples()
    DAO-->>Repository: LiveData<List<HttpTransactionTuple>>
```

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt:83-99](), [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionDatabaseRepository.kt:23-46]()

## Integration Points

The Core API serves as the bridge between external client code and Chucker's internal systems:

### External Integration Points

| Integration Point | Purpose | Implementation |
|------------------|---------|----------------|
| **UI Launch** | Start Chucker interface | `getLaunchIntent()` creates `MainActivity` intent |
| **Notification Management** | Control system notifications | `dismissNotifications()` delegates to `NotificationHelper` |
| **Data Lifecycle** | Manage transaction storage | `clearTransactions()` operates through repository layer |
| **Data Export** | Extract HTTP data | `generateHar()` combines repository queries with HAR formatting |

### Internal System Dependencies

The Core API depends on several internal systems:

- **RepositoryProvider**: Factory for accessing data repositories
- **NotificationHelper**: Android notification management
- **HarUtils**: HTTP Archive format generation
- **MainActivity**: Primary UI entry point
- **Logger**: Internal logging infrastructure

**Sources:** [library/src/main/kotlin/com/chuckerteam/chucker/api/Chucker.kt:13-18](), [library/src/main/kotlin/com/chuckerteam/chucker/internal/data/repository/HttpTransactionDatabaseRepository.kt:6-7]()
