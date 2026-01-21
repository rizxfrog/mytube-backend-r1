# MyTube Backend

Multi-module Gradle build for the MyTube backend. It contains shared libraries,
Spring Boot services, and a GraphQL gateway.

## Modules
- mytube-common
- mytube-api
- mytube-gateway-graphql
- mytube-user-service
- mytube-video-service
- mytube-comment-service
- mytube-favorite-service
- mytube-danmu-service
- mytube-im-service
- mytube-search-service
- mytube-upload-service
- mytube-stats-service

## Requirements
- Java 23 (see root toolchain in `build.gradle.kts`)
- Local Gradle installation (no wrapper in this repo)

## Build
```bash
gradle build
```

## Run a service
```bash
gradle :mytube-video-service:bootRun
```

## Test a module
```bash
gradle :mytube-video-service:test
```

## Configuration
Service configs live in each module:
- `src/main/resources/application.yml`
- `src/main/resources/bootstrap.yml`

Database migrations are under:
- `src/main/resources/db/migration`

External dependencies commonly used in this repo include Nacos, PostgreSQL,
Redis, Flyway, and OSS clients. Keep secrets out of the repo and use
environment variables or local overrides.

## API
See `API.md` for the REST and GraphQL endpoint list.
