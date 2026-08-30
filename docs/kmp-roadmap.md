# Kotlin Multiplatform roadmap

## Status

This repository is currently a JVM-only API automation framework. It does **not** implement Kotlin Multiplatform (KMP). This document records a possible evolution path, not a delivered feature.

## Reusable candidates

The following code is largely platform-neutral and could be moved into a future shared module after dependency review:

- request and response DTOs;
- the sealed `ApiResponse` contract;
- domain-level validation helpers.

## Required changes

| Current JVM choice | KMP direction | Reason |
|---|---|---|
| REST Assured | Ktor Client | REST Assured is JVM-only. |
| Jackson annotations and mapper | `kotlinx.serialization` | Provides cross-platform serialization. |
| Owner configuration | platform-specific configuration adapters | Environment and secret access differ by platform. |
| DataFaker | platform-neutral generator or `expect`/`actual` implementation | Data generation must work across targets. |
| TestNG and Allure TestNG | target-specific runners and reporting adapters | Test execution differs across JVM, iOS, Android, and web targets. |

## Proposed shape

```text
shared/
├── commonMain/  # DTOs, response contract, Ktor client abstractions
├── jvmMain/     # JVM HTTP engine and TestNG/Allure adapters
└── iosMain/     # Darwin HTTP engine and XCTest adapter
```

## Migration guardrails

1. Start by extracting DTOs and serialization tests; do not migrate test runners first.
2. Keep the existing JVM suite green while introducing shared modules.
3. Validate wire compatibility between Jackson and `kotlinx.serialization` with contract fixtures.
4. Add a second target only after the shared client has meaningful reuse.
