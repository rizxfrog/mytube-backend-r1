# Repository Guidelines

## Project Structure & Module Organization
This is a multi-module Gradle build with Spring Boot services.

- Root build configuration: `build.gradle.kts`, `settings.gradle.kts`.
- Shared libraries: `mytube-common`, `mytube-api`.
- Service modules: `mytube-*-service` (user, video, comment, favorite, danmu, im, search, upload, stats).
- Gateway: `mytube-gateway-graphql`.
- Service config: `src/main/resources/application.yml` and `bootstrap.yml` in each module.
- Database migrations: `src/main/resources/db/migration` (Flyway).

## Build, Test, and Development Commands
Gradle wrapper is not present; use a local Gradle installation.

- `gradle build` — compile all modules and run tests (if present).
- `gradle :mytube-video-service:bootRun` — run a specific service locally.
- `gradle :mytube-video-service:test` — run tests for one module (add tests first).

## Coding Style & Naming Conventions
- Java 4-space indentation; keep line lengths reasonable.
- Packages follow `com.mytube.<service>` (e.g., `com.mytube.video`).
- Classes use `PascalCase`, methods/fields use `camelCase`.
- Prefer clear DTO/DAO naming patterns seen in codebase (`VideoDTO`, `VideoDAO`).
- No explicit formatter config found; match surrounding style.

## Testing Guidelines
- Testing is via `spring-boot-starter-test` (JUnit 5 + Spring Test).
- Place tests under `src/test/java` and mirror package names.
- Naming: `*Test` suffix (e.g., `VideoServiceTest`).
- There are currently no test sources detected; add module-level tests as needed.

## Commit & Pull Request Guidelines
- Git history is minimal (single short message: `frame`), so no established convention.
- Use short, imperative commit subjects (e.g., `add video query caching`).
- PRs should include a concise description, linked issues if applicable, and test notes.

## Configuration & Environment Notes
- Service configs live in each module’s `application.yml`/`bootstrap.yml`.
- External dependencies include Nacos, PostgreSQL, Redis, Flyway, and OSS clients.
- Keep secrets out of the repo; use environment variables or local config overrides.
