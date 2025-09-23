# Contributing

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [.github/ISSUE_TEMPLATE/bug_report.md](.github/ISSUE_TEMPLATE/bug_report.md)
- [.github/ISSUE_TEMPLATE/feature_request.md](.github/ISSUE_TEMPLATE/feature_request.md)
- [.github/PULL_REQUEST_TEMPLATE](.github/PULL_REQUEST_TEMPLATE)

</details>



This document provides comprehensive guidelines for contributing to the Chucker HTTP inspection library. It covers the development workflow, issue reporting processes, pull request procedures, and quality standards expected from contributors.

For information about the build system setup, see [Build System](#6.1). For details about automated testing and CI/CD processes, see [CI/CD Pipeline](#6.2).

## Contribution Workflow Overview

The Chucker project follows a structured contribution model that integrates with the build system and automated quality gates to ensure code quality and project stability.

### Overall Contribution Process

```mermaid
flowchart TD
    CONTRIBUTOR["Contributor"] --> ISSUE_CHOICE{"Contribution Type"}
    
    ISSUE_CHOICE --> BUG_REPORT["Bug Report"]
    ISSUE_CHOICE --> FEATURE_REQ["Feature Request"]
    ISSUE_CHOICE --> DIRECT_PR["Direct Pull Request"]
    
    BUG_REPORT --> BUG_TEMPLATE[".github/ISSUE_TEMPLATE/bug_report.md"]
    FEATURE_REQ --> FEATURE_TEMPLATE[".github/ISSUE_TEMPLATE/feature_request.md"]
    
    BUG_TEMPLATE --> ISSUE_CREATED["GitHub Issue Created"]
    FEATURE_TEMPLATE --> ISSUE_CREATED
    DIRECT_PR --> FORK_REPO["Fork Repository"]
    
    ISSUE_CREATED --> DISCUSSION["Community Discussion"]
    DISCUSSION --> APPROVED{"Issue Approved?"}
    
    APPROVED -->|Yes| FORK_REPO
    APPROVED -->|No| ISSUE_CLOSED["Issue Closed"]
    
    FORK_REPO --> LOCAL_DEV["Local Development"]
    LOCAL_DEV --> PR_CREATED["Pull Request Created"]
    
    PR_CREATED --> PR_TEMPLATE[".github/PULL_REQUEST_TEMPLATE"]
    PR_TEMPLATE --> CI_PIPELINE["CI/CD Pipeline Execution"]
    
    CI_PIPELINE --> QUALITY_GATES["Quality Gates"]
    QUALITY_GATES --> CODE_REVIEW["Code Review"]
    
    CODE_REVIEW --> REVIEW_RESULT{"Review Status"}
    REVIEW_RESULT -->|Changes Requested| LOCAL_DEV
    REVIEW_RESULT -->|Approved| MERGE_PR["Merge Pull Request"]
    
    MERGE_PR --> RELEASE_PROCESS["Release Process"]
```

Sources: [.github/PULL_REQUEST_TEMPLATE](), [.github/ISSUE_TEMPLATE/bug_report.md](), [.github/ISSUE_TEMPLATE/feature_request.md]()

## Issue Reporting Process

Chucker uses structured issue templates to ensure consistent and actionable bug reports and feature requests.

### Bug Reporting Workflow

```mermaid
flowchart TD
    USER["User Encounters Issue"] --> BUG_TEMPLATE["bug_report.md Template"]
    
    BUG_TEMPLATE --> REQUIRED_FIELDS["Required Information"]
    
    REQUIRED_FIELDS --> BUG_DESC["Bug Description"]
    REQUIRED_FIELDS --> REPRO_STEPS["Reproduction Steps"]
    REQUIRED_FIELDS --> EXPECTED_BEHAVIOR["Expected Behavior"]
    REQUIRED_FIELDS --> SCREENSHOTS["Screenshots"]
    REQUIRED_FIELDS --> TECH_INFO["Technical Information"]
    
    TECH_INFO --> DEVICE_INFO["Device Model"]
    TECH_INFO --> OS_VERSION["Android OS Version"]
    TECH_INFO --> CHUCKER_VERSION["Chucker Library Version"]
    
    BUG_DESC --> ISSUE_VALIDATION["Issue Validation"]
    REPRO_STEPS --> ISSUE_VALIDATION
    EXPECTED_BEHAVIOR --> ISSUE_VALIDATION
    SCREENSHOTS --> ISSUE_VALIDATION
    DEVICE_INFO --> ISSUE_VALIDATION
    OS_VERSION --> ISSUE_VALIDATION
    CHUCKER_VERSION --> ISSUE_VALIDATION
    
    ISSUE_VALIDATION --> TRIAGE["Maintainer Triage"]
    TRIAGE --> LABELS["Apply Labels"]
    LABELS --> ASSIGNMENT["Issue Assignment"]
```

| Field | Purpose | Required |
|-------|---------|----------|
| Bug Description | Clear explanation of the issue | Yes |
| Reproduction Steps | Step-by-step instructions to reproduce | Yes |
| Expected Behavior | What should happen instead | Yes |
| Screenshots | Visual evidence of the issue | Optional |
| Device | Specific device model | Yes |
| OS Version | Android version | Yes |
| Chucker Version | Library version number | Yes |

Sources: [.github/ISSUE_TEMPLATE/bug_report.md:7-29]()

### Feature Request Process

```mermaid
flowchart TD
    CONTRIBUTOR["Contributor"] --> FEATURE_TEMPLATE["feature_request.md Template"]
    
    FEATURE_TEMPLATE --> PROBLEM_DESC["Problem Description"]
    FEATURE_TEMPLATE --> SOLUTION_DESC["Proposed Solution"]
    FEATURE_TEMPLATE --> ALTERNATIVES["Alternative Solutions"]
    FEATURE_TEMPLATE --> CONTEXT["Additional Context"]
    FEATURE_TEMPLATE --> SELF_DEVELOP["Self-Development Intent"]
    
    PROBLEM_DESC --> VALIDATION["Feature Validation"]
    SOLUTION_DESC --> VALIDATION
    ALTERNATIVES --> VALIDATION
    CONTEXT --> VALIDATION
    
    VALIDATION --> COMMUNITY_FEEDBACK["Community Feedback"]
    COMMUNITY_FEEDBACK --> MAINTAINER_REVIEW["Maintainer Review"]
    
    MAINTAINER_REVIEW --> DECISION{"Feature Decision"}
    DECISION -->|Approved| IMPLEMENTATION_PLAN["Implementation Planning"]
    DECISION -->|Rejected| REJECTION_RATIONALE["Rejection Explanation"]
    
    SELF_DEVELOP --> CONTRIBUTOR_ASSIGNMENT["Contributor Assignment"]
    IMPLEMENTATION_PLAN --> CONTRIBUTOR_ASSIGNMENT
    IMPLEMENTATION_PLAN --> MAINTAINER_ASSIGNMENT["Maintainer Assignment"]
    
    CONTRIBUTOR_ASSIGNMENT --> DEVELOPMENT["Development Phase"]
    MAINTAINER_ASSIGNMENT --> DEVELOPMENT
```

Sources: [.github/ISSUE_TEMPLATE/feature_request.md:7-22]()

## Pull Request Process

The pull request workflow ensures code quality through structured templates and automated validation.

### Pull Request Template Structure

```mermaid
flowchart TD
    PR_CREATION["Pull Request Created"] --> PR_TEMPLATE["PULL_REQUEST_TEMPLATE"]
    
    PR_TEMPLATE --> SCREENSHOTS_SECTION["Screenshots Section"]
    PR_TEMPLATE --> CONTEXT_SECTION["Context Section"]
    PR_TEMPLATE --> CHANGES_SECTION["Changes Section"]
    PR_TEMPLATE --> RELATED_PR_SECTION["Related PR Section"]
    PR_TEMPLATE --> BREAKING_SECTION["Breaking Changes Section"]
    PR_TEMPLATE --> TESTING_SECTION["Testing Instructions"]
    PR_TEMPLATE --> NEXT_STEPS_SECTION["Next Steps Section"]
    
    CONTEXT_SECTION --> ISSUE_LINKING["Link to GitHub Issues"]
    CONTEXT_SECTION --> EXTERNAL_REFS["External References"]
    
    CHANGES_SECTION --> CODE_CHANGES["Code Modifications"]
    CHANGES_SECTION --> CHANGELOG_UPDATE["CHANGELOG.md Update"]
    
    BREAKING_SECTION --> API_CHANGES["API Signature Changes"]
    BREAKING_SECTION --> METHOD_CHANGES["Method Changes"]
    
    SCREENSHOTS_SECTION --> UI_CHANGES["UI Modifications Visual"]
    TESTING_SECTION --> SPECIAL_TESTING["Special Test Cases"]
    NEXT_STEPS_SECTION --> POST_MERGE_TASKS["Post-Merge Planning"]
```

| Section | Purpose | Required |
|---------|---------|----------|
| Screenshots | Visual demonstration of changes | For UI changes |
| Context | Explanation and issue linking | Yes |
| Changes | Code modifications description | Yes |
| Related PR | Dependencies and blocking PRs | If applicable |
| Breaking | API compatibility impact | If applicable |
| Testing | Special testing requirements | If complex |
| Next Steps | Post-merge planning | If applicable |

Sources: [.github/PULL_REQUEST_TEMPLATE:1-22]()

### Pull Request Validation Flow

```mermaid
flowchart TD
    PR_SUBMITTED["Pull Request Submitted"] --> TEMPLATE_CHECK["Template Completeness Check"]
    
    TEMPLATE_CHECK --> CI_TRIGGER["GitHub Actions Triggered"]
    CI_TRIGGER --> PARALLEL_CHECKS["Parallel Quality Checks"]
    
    PARALLEL_CHECKS --> UNIT_TESTS["Unit Tests"]
    PARALLEL_CHECKS --> DETEKT_CHECK["Detekt Static Analysis"]
    PARALLEL_CHECKS --> LINT_CHECK["Android Lint"]
    PARALLEL_CHECKS --> KTLINT_CHECK["Kotlin Style Check"]
    PARALLEL_CHECKS --> API_CHECK["Binary Compatibility Check"]
    
    UNIT_TESTS --> TEST_RESULTS["Test Results"]
    DETEKT_CHECK --> STATIC_ANALYSIS_RESULTS["Static Analysis Results"]
    LINT_CHECK --> LINT_RESULTS["Lint Results"]
    KTLINT_CHECK --> STYLE_RESULTS["Style Check Results"]
    API_CHECK --> COMPATIBILITY_RESULTS["API Compatibility Results"]
    
    TEST_RESULTS --> QUALITY_GATE["Quality Gate Evaluation"]
    STATIC_ANALYSIS_RESULTS --> QUALITY_GATE
    LINT_RESULTS --> QUALITY_GATE
    STYLE_RESULTS --> QUALITY_GATE
    COMPATIBILITY_RESULTS --> QUALITY_GATE
    
    QUALITY_GATE --> GATE_STATUS{"All Checks Pass?"}
    GATE_STATUS -->|Yes| READY_FOR_REVIEW["Ready for Review"]
    GATE_STATUS -->|No| FAILED_CHECKS["Failed Checks Reported"]
    
    FAILED_CHECKS --> CONTRIBUTOR_FIXES["Contributor Fixes Issues"]
    CONTRIBUTOR_FIXES --> CI_TRIGGER
    
    READY_FOR_REVIEW --> MAINTAINER_REVIEW["Maintainer Code Review"]
    MAINTAINER_REVIEW --> REVIEW_OUTCOME{"Review Decision"}
    
    REVIEW_OUTCOME -->|Approved| MERGE_ELIGIBLE["Eligible for Merge"]
    REVIEW_OUTCOME -->|Changes Requested| CONTRIBUTOR_FIXES
    
    MERGE_ELIGIBLE --> MERGE_STRATEGY["Merge Strategy Selection"]
    MERGE_STRATEGY --> MERGED_PR["Pull Request Merged"]
```

Sources: [.github/PULL_REQUEST_TEMPLATE:9]()

## Development Workflow Integration

The contribution process integrates with Chucker's modular architecture and build system to maintain code quality across all components.

### Repository Structure and Contribution Areas

```mermaid
flowchart TD
    CHUCKER_REPO["ChuckerTeam/chucker Repository"] --> MODULE_AREAS["Contribution Areas"]
    
    MODULE_AREAS --> LIBRARY_MODULE["library/ Module"]
    MODULE_AREAS --> NOOP_MODULE["library-no-op/ Module"]
    MODULE_AREAS --> SAMPLE_MODULE["sample/ Module"]
    MODULE_AREAS --> BUILD_SYSTEM["Build System Files"]
    MODULE_AREAS --> CI_CONFIG["CI/CD Configuration"]
    MODULE_AREAS --> DOCUMENTATION["Documentation"]
    
    LIBRARY_MODULE --> CORE_API["Core API Components"]
    LIBRARY_MODULE --> INTERCEPTOR["ChuckerInterceptor"]
    LIBRARY_MODULE --> COLLECTOR["ChuckerCollector"]
    LIBRARY_MODULE --> UI_COMPONENTS["UI Activities & Fragments"]
    LIBRARY_MODULE --> DATA_LAYER["Data Repository & DAO"]
    
    NOOP_MODULE --> STUB_IMPL["Stub Implementations"]
    
    SAMPLE_MODULE --> DEMO_APP["Demo Application"]
    SAMPLE_MODULE --> INTEGRATION_EXAMPLES["Integration Examples"]
    
    BUILD_SYSTEM --> GRADLE_FILES["Gradle Build Scripts"]
    BUILD_SYSTEM --> PUBLISHING_CONFIG["Publishing Configuration"]
    
    CI_CONFIG --> GITHUB_ACTIONS["GitHub Actions Workflows"]
    CI_CONFIG --> QUALITY_CHECKS["Quality Check Definitions"]
    
    DOCUMENTATION --> README["README.md"]
    DOCUMENTATION --> CHANGELOG["CHANGELOG.md"]
    DOCUMENTATION --> ISSUE_TEMPLATES["Issue Templates"]
    DOCUMENTATION --> PR_TEMPLATE["PR Template"]
```

### Contribution Impact Assessment

| Component | Impact Level | Review Requirements |
|-----------|--------------|-------------------|
| `library/` Core API | High | Maintainer + Community Review |
| `library/` UI Components | Medium | Maintainer Review + Screenshots |
| `library-no-op/` Stubs | High | API Compatibility Check |
| `sample/` Demo App | Low | Basic Review |
| Build System | High | Build System Expert Review |
| CI/CD Configuration | High | Maintainer Review |
| Documentation | Low | Community Review |

Sources: [.github/PULL_REQUEST_TEMPLATE:14-15]()

## Code Review and Quality Standards

The project maintains high code quality through structured review processes and automated quality gates.

### Review Process Flow

```mermaid
sequenceDiagram
    participant C as "Contributor"
    participant GH as "GitHub"
    participant CI as "CI/CD Pipeline"
    participant M as "Maintainer"
    participant COMM as "Community"
    
    C->>GH: "Submit Pull Request"
    GH->>CI: "Trigger Automated Checks"
    
    CI->>CI: "Run Quality Gates"
    CI->>GH: "Report Check Results"
    
    alt All Checks Pass
        GH->>M: "Notify for Review"
        M->>GH: "Perform Code Review"
        
        alt Simple Change
            M->>GH: "Approve and Merge"
        else Complex Change
            M->>COMM: "Request Community Input"
            COMM->>M: "Provide Feedback"
            M->>GH: "Final Review Decision"
        end
    else Checks Fail
        GH->>C: "Notify of Failed Checks"
        C->>C: "Fix Issues"
        C->>GH: "Update Pull Request"
    end
    
    Note over C,COMM: "Iterative Process Until Approved"
```

### Quality Gate Requirements

| Check Type | Tool/Process | Failure Impact |
|------------|--------------|----------------|
| Unit Tests | JUnit | Blocks merge |
| Static Analysis | Detekt | Blocks merge |
| Code Style | ktlint | Blocks merge |
| Android Lint | Android Lint | Blocks merge |
| API Compatibility | Binary Compatibility Validator | Blocks merge |
| Manual Review | Maintainer Review | Blocks merge |

Sources: [.github/PULL_REQUEST_TEMPLATE:17-18]()

## Changelog and Release Integration

Contributors must update the changelog for user-facing changes to ensure proper release documentation.

### Changelog Update Process

```mermaid
flowchart TD
    CODE_CHANGE["Code Change Implemented"] --> CHANGE_TYPE{"Change Type Assessment"}
    
    CHANGE_TYPE --> NEW_FEATURE["New Feature"]
    CHANGE_TYPE --> BUG_FIX["Bug Fix"]
    CHANGE_TYPE --> API_CHANGE["API Modification"]
    CHANGE_TYPE --> INTERNAL_CHANGE["Internal Refactoring"]
    
    NEW_FEATURE --> UPDATE_CHANGELOG["Update CHANGELOG.md"]
    BUG_FIX --> UPDATE_CHANGELOG
    API_CHANGE --> UPDATE_CHANGELOG
    INTERNAL_CHANGE --> NO_CHANGELOG["No Changelog Update"]
    
    UPDATE_CHANGELOG --> UNRELEASED_SECTION["Add to Unreleased Section"]
    UNRELEASED_SECTION --> CATEGORY_SELECTION["Select Category"]
    
    CATEGORY_SELECTION --> ADDED_SECTION["Added"]
    CATEGORY_SELECTION --> CHANGED_SECTION["Changed"]
    CATEGORY_SELECTION --> DEPRECATED_SECTION["Deprecated"]
    CATEGORY_SELECTION --> REMOVED_SECTION["Removed"]
    CATEGORY_SELECTION --> FIXED_SECTION["Fixed"]
    CATEGORY_SELECTION --> SECURITY_SECTION["Security"]
    
    ADDED_SECTION --> PR_SUBMISSION["Include in PR"]
    CHANGED_SECTION --> PR_SUBMISSION
    DEPRECATED_SECTION --> PR_SUBMISSION
    REMOVED_SECTION --> PR_SUBMISSION
    FIXED_SECTION --> PR_SUBMISSION
    SECURITY_SECTION --> PR_SUBMISSION
    
    NO_CHANGELOG --> PR_SUBMISSION
```

Sources: [.github/PULL_REQUEST_TEMPLATE:9]()
