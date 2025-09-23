# Build System

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [.editorconfig](.editorconfig)
- [.gitattributes](.gitattributes)
- [.gitignore](.gitignore)
- [build.gradle](build.gradle)
- [gradle/gradle-mvn-push.gradle](gradle/gradle-mvn-push.gradle)
- [gradle/wrapper/gradle-wrapper.jar](gradle/wrapper/gradle-wrapper.jar)
- [gradle/wrapper/gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties)
- [gradlew](gradlew)
- [gradlew.bat](gradlew.bat)
- [library-no-op/build.gradle](library-no-op/build.gradle)
- [library/build.gradle](library/build.gradle)
- [library/src/main/kotlin/com/chuckerteam/chucker/internal/ui/BaseChuckerActivity.kt](library/src/main/kotlin/com/chuckerteam/chucker/internal/ui/BaseChuckerActivity.kt)
- [sample/build.gradle](sample/build.gradle)
- [sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt](sample/src/main/kotlin/com/chuckerteam/chucker/sample/MainActivity.kt)

</details>



This document covers Chucker's Gradle-based build system, including multi-module architecture, dependency management, build variants strategy, and artifact publishing workflows. For information about continuous integration and deployment pipelines, see [CI/CD Pipeline](#6.2). For development workflow and contribution guidelines, see [Contributing](#6.3).

## Module Architecture

Chucker uses a three-module Gradle build structure designed to provide zero-overhead release builds while maintaining full debugging capabilities during development.

### Module Structure

```mermaid
graph TD
    subgraph "Root Project"
        ROOT["build.gradle<br/>Project Configuration"]
    end
    
    subgraph "Core Modules"
        LIB["library/<br/>Full Implementation"]
        NOOP["library-no-op/<br/>Stub Implementation"]
        SAMPLE["sample/<br/>Demo Application"]
    end
    
    subgraph "Build Configuration"
        WRAPPER["gradle/wrapper/<br/>Gradle Distribution"]
        PUSH["gradle/gradle-mvn-push.gradle<br/>Maven Central Publishing"]
    end
    
    ROOT --> LIB
    ROOT --> NOOP  
    ROOT --> SAMPLE
    ROOT --> WRAPPER
    ROOT --> PUSH
    
    SAMPLE -->|"debugImplementation"| LIB
    SAMPLE -->|"releaseImplementation"| NOOP
```

**Module Dependencies and Build Variants**

Sources: [sample/build.gradle:70-71](), [build.gradle:1-118](), [library/build.gradle:1-157](), [library-no-op/build.gradle:1-109]()

### Build Variants Strategy

The build system implements a dual-library approach where the same public API is provided by two different implementations:

| Build Type | Dependency | Behavior |
|------------|------------|----------|
| Debug | `library` | Full HTTP inspection, UI, persistence |
| Release | `library-no-op` | Empty stubs, zero runtime overhead |

```mermaid
flowchart LR
    subgraph "Consumer App Build"
        DEBUG["Debug Build"]
        RELEASE["Release Build"]
    end
    
    subgraph "Chucker Libraries"
        FULL["library module<br/>ChuckerInterceptor<br/>ChuckerCollector<br/>UI Activities<br/>Room Database"]
        STUB["library-no-op module<br/>Empty ChuckerInterceptor<br/>Empty ChuckerCollector<br/>No UI<br/>No Database"]
    end
    
    DEBUG -->|"debugImplementation"| FULL
    RELEASE -->|"releaseImplementation"| STUB
```

**Implementation Details**

Sources: [sample/build.gradle:70-71](), [library-no-op/build.gradle:43-46]()

## Build Configuration

### Root Project Configuration

The root `build.gradle` establishes the foundation for all modules with centralized version management and plugin configuration.

**Key Configuration Elements:**

| Component | Purpose | Configuration |
|-----------|---------|---------------|
| Buildscript | Plugin dependencies and versions | [build.gradle:1-62]() |
| Version Management | Centralized dependency versions | [build.gradle:2-44]() |
| Repository Setup | Maven/Google/JFrog repositories | [build.gradle:46-92]() |
| SDK Configuration | Min/Target/Compile SDK versions | [build.gradle:112-116]() |

```mermaid
graph TD
    subgraph "Build Configuration Flow"
        BUILDSCRIPT["buildscript block<br/>Plugin Dependencies"]
        ALLPROJECTS["allprojects block<br/>Repository Configuration"]
        VERSIONS["ext block<br/>Version Definitions"]
        PLUGINS["Applied Plugins<br/>artifactory, maven-publish"]
    end
    
    BUILDSCRIPT --> ALLPROJECTS
    VERSIONS --> BUILDSCRIPT
    ALLPROJECTS --> PLUGINS
    
    subgraph "Module Inheritance"
        LIB_CONFIG["library/build.gradle<br/>Inherits versions & repos"]
        NOOP_CONFIG["library-no-op/build.gradle<br/>Inherits versions & repos"] 
        SAMPLE_CONFIG["sample/build.gradle<br/>Inherits versions & repos"]
    end
    
    PLUGINS --> LIB_CONFIG
    PLUGINS --> NOOP_CONFIG
    PLUGINS --> SAMPLE_CONFIG
```

**Global Build Properties**

Sources: [build.gradle:112-116](), [build.gradle:2-44]()

### Module-Specific Configurations

#### Library Module

The main `library` module contains the full Chucker implementation with comprehensive Android library configuration.

**Key Features:**
- Kotlin compilation with explicit API mode: [library/build.gradle:11-14]()
- ViewBinding and Room database integration: [library/build.gradle:27-30](), [library/build.gradle:69-71]()
- Comprehensive dependency set including Material Design, OkHttp, Room: [library/build.gradle:58-94]()
- Resource prefix enforcement: [library/build.gradle:55]()

#### No-Op Module  

The `library-no-op` module provides stub implementations with minimal dependencies.

**Minimal Configuration:**
- Only essential dependencies (OkHttp, Kotlin): [library-no-op/build.gradle:43-46]()
- No ViewBinding, Room, or UI dependencies
- Same public API surface as full library
- Identical publishing configuration: [library-no-op/build.gradle:67-108]()

Sources: [library/build.gradle:1-157](), [library-no-op/build.gradle:1-109]()

## Dependency Management

### Version Centralization

All dependency versions are centralized in the root build file's `ext` block, enabling consistent version management across modules.

**Major Dependency Categories:**

| Category | Key Dependencies | Version Variables |
|----------|------------------|-------------------|
| Kotlin | kotlin-stdlib, coroutines | `kotlinVersion`, `coroutineVersion` |
| Android | AppCompat, Material, Room | `appCompatVersion`, `materialComponentsVersion`, `roomVersion` |
| Networking | OkHttp, Retrofit | `okhttpVersion`, `retrofitVersion` |
| Testing | JUnit, MockK, Truth | `junitVersion`, `mockkVersion`, `truthVersion` |
| Quality | Detekt, KtLint, Dokka | `ktLintVersion`, `dokkaVersion` |

```mermaid
graph LR
    subgraph "Dependency Resolution"
        ROOT_EXT["Root build.gradle ext block<br/>Version Definitions"]
        
        subgraph "Module Dependencies"
            LIB_DEPS["library dependencies<br/>Full Feature Set"]
            NOOP_DEPS["library-no-op dependencies<br/>Minimal Set"]
            SAMPLE_DEPS["sample dependencies<br/>Demo Requirements"]
        end
        
        ROOT_EXT --> LIB_DEPS
        ROOT_EXT --> NOOP_DEPS  
        ROOT_EXT --> SAMPLE_DEPS
    end
    
    subgraph "External Sources"
        GOOGLE["google()"]
        MAVEN["mavenCentral()"]
        JFROG["JFrog Artifactory"]
    end
    
    LIB_DEPS --> GOOGLE
    LIB_DEPS --> MAVEN
    LIB_DEPS --> JFROG
```

**Dependency Categories**

Sources: [build.gradle:2-44](), [library/build.gradle:58-94](), [library-no-op/build.gradle:43-46]()

## Publishing and Distribution

Chucker implements a dual publishing strategy targeting both internal JFrog Artifactory and public Maven Central repositories.

### Publishing Architecture

```mermaid
graph TD
    subgraph "Artifact Generation"
        LIB_AAR["library.aar<br/>Full Implementation"]
        NOOP_AAR["library-no-op.aar<br/>Stub Implementation"]
        SOURCES["androidSourcesJar<br/>Source Code"]
        JAVADOC["javadocJar<br/>API Documentation"]
    end
    
    subgraph "Publishing Targets"
        JFROG_SNAPSHOT["JFrog Artifactory<br/>Snapshot Repository"]
        JFROG_RELEASE["JFrog Artifactory<br/>Release Repository"] 
        MAVEN_SNAPSHOT["Maven Central<br/>Sonatype Snapshots"]
        MAVEN_STAGING["Maven Central<br/>Sonatype Staging"]
    end
    
    LIB_AAR --> JFROG_SNAPSHOT
    LIB_AAR --> JFROG_RELEASE
    LIB_AAR --> MAVEN_SNAPSHOT
    LIB_AAR --> MAVEN_STAGING
    
    NOOP_AAR --> JFROG_SNAPSHOT
    NOOP_AAR --> JFROG_RELEASE
    NOOP_AAR --> MAVEN_SNAPSHOT  
    NOOP_AAR --> MAVEN_STAGING
    
    SOURCES --> MAVEN_SNAPSHOT
    SOURCES --> MAVEN_STAGING
    JAVADOC --> MAVEN_SNAPSHOT
    JAVADOC --> MAVEN_STAGING
```

**Publishing Configuration**

### JFrog Artifactory Publishing

Both `library` and `library-no-op` modules configure JFrog Artifactory publishing for internal distribution:

**Configuration Elements:**
- Repository selection based on version (snapshot vs release): [library/build.gradle:143-144]()
- AAR artifact publishing with dependency resolution: [library/build.gradle:118-136]()
- Credential management via project properties: [library/build.gradle:140-147]()

### Maven Central Publishing  

The `gradle-mvn-push.gradle` script configures public Maven Central publishing:

**Key Features:**
- Dokka integration for API documentation: [gradle/gradle-mvn-push.gradle:8-29]()
- PGP signing for release artifacts: [gradle/gradle-mvn-push.gradle:108-118]()
- Complete POM metadata with developer information: [gradle/gradle-mvn-push.gradle:67-103]()
- Sonatype repository configuration: [gradle/gradle-mvn-push.gradle:44-60]()

Sources: [library/build.gradle:115-156](), [library-no-op/build.gradle:67-108](), [gradle/gradle-mvn-push.gradle:1-119]()

## Build Automation and Tooling

### Gradle Wrapper Configuration

Chucker uses Gradle Wrapper to ensure consistent build environments across development machines and CI systems.

**Wrapper Configuration:**
- Gradle version: 8.11.1 (all distribution): [gradle/wrapper/gradle-wrapper.properties:4]()
- Wrapper scripts for Unix and Windows: [gradlew:1-235](), [gradlew.bat:1-90]()

### Quality Gates and Plugins

The build system integrates multiple quality control plugins:

| Plugin | Purpose | Configuration Location |
|--------|---------|----------------------|
| `org.jlleitschuh.gradle.ktlint` | Kotlin code formatting | [build.gradle:57]() |
| `de.mannodermaus.gradle.plugins.android-junit5` | JUnit 5 support | [build.gradle:54]() |
| `org.jetbrains.dokka.dokka-gradle-plugin` | API documentation | [build.gradle:56]() |
| `org.jetbrains.kotlinx.binary-compatibility-validator` | API compatibility | [build.gradle:59]() |
| `com.squareup.wire.wire-gradle-plugin` | Protocol Buffers | [build.gradle:60]() |

```mermaid
graph LR
    subgraph "Build Quality Pipeline"
        COMPILE["Kotlin Compilation<br/>Explicit API Mode"]
        LINT["Android Lint<br/>Warnings as Errors"]
        KTLINT["KtLint<br/>Code Formatting"]
        TEST["JUnit 5 Tests<br/>Unit & Integration"]
        API_CHECK["API Compatibility<br/>Binary Validation"]
    end
    
    COMPILE --> LINT
    LINT --> KTLINT  
    KTLINT --> TEST
    TEST --> API_CHECK
    
    subgraph "Build Outputs"
        SUCCESS["Build Success<br/>Ready for Publishing"]
        FAILURE["Build Failure<br/>Quality Gate Failed"]
    end
    
    API_CHECK --> SUCCESS
    API_CHECK --> FAILURE
```

**Build Tasks and Hooks**

The build system includes custom tasks for setup and maintenance:
- Git hook installation: [build.gradle:101-105]()
- Clean task with hook dependency: [build.gradle:107-110]()
- Test output configuration: [build.gradle:94-98]()

Sources: [build.gradle:52-61](), [library/build.gradle:32-48](), [build.gradle:101-110]()
