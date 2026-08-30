# Engineering decisions

## Kotlin and typed API responses

The framework uses Kotlin data classes for DTOs and a sealed `ApiResponse` type for HTTP outcomes. Tests can call `assertSuccess()` and receive a typed model; failures retain the status code and response body for diagnosis. This keeps transport errors explicit instead of scattering nullable response handling through test code.

## A client layer between tests and REST Assured

Test classes do not assemble HTTP calls directly. `AuthApiClient` and `PlayerApiClient` own endpoint paths, serialization, authorization, and Allure steps. This separates scenario intent from transport details and localizes changes when the API contract evolves.

## Parallel creation with controlled shared state

The player-creation stage creates twelve generated players through a parallel stream. `TestContext` uses `CopyOnWriteArrayList` to collect created records safely, then dependent TestNG groups read, delete, and verify those same records. The design gives useful concurrency while preserving a deterministic CRUD lifecycle.

## Deterministic cleanup

The suite only verifies and deletes records it created. Generated data avoids collisions between runs; the final verification confirms cleanup rather than assuming a delete call succeeded. This prevents unrelated environment data from becoming an assertion target.

## Configuration outside source control

Owner loads environment-specific configuration with an explicit precedence order. Local credentials belong in an ignored file; CI credentials arrive through GitHub Actions secrets. No credential values are stored in repository configuration or workflow files.

## Reporting as diagnostic evidence

Allure steps are attached at the API-client boundary, where request intent and HTTP responses meet. Gradle test reports and Allure result files are published as CI artifacts so a failed run can be investigated without reproducing it immediately.

## Current scope

This is a JVM framework built around REST Assured and TestNG. Kotlin Multiplatform is intentionally not presented as an implemented capability; the migration considerations are documented separately in [kmp-roadmap.md](kmp-roadmap.md).
