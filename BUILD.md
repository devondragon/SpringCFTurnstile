# Build and Publish Command Reference

This document provides commands for building, testing, and publishing the Spring Cloudflare Turnstile library.

## Building and Testing

### Full Build with Tests

Run a complete build with all tests (JDK 17 and 21):

```shell
./gradlew build
```

### Run Tests Only

Run all tests:

```shell
./gradlew test
```

Run tests on a specific JDK version:

```shell
./gradlew testJdk17
./gradlew testJdk21
```

## Publishing

### Publish to Local Maven Repository

Use this during development to test integration with other local projects:

```shell
./gradlew publishToMavenLocal --refresh-dependencies
```

Or using the alias:

```shell
./gradlew publishLocal
```

### Publish to Private Maven Repository

Publish to the Reposilite private repository:

```shell
./gradlew publishReposilite
```

### Publish to Maven Central

This will publish the library to Maven Central:

```shell
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

Or using the alias:

```shell
./gradlew publishMavenCentral
```

## Versioning and Release

This project uses the [Gradle Release Plugin](https://github.com/researchgate/gradle-release) to manage versioning and releases.

### Creating a New Release

To create a new release with an incremented version:

```shell
./gradlew release
```

This will:
1. Check that there are no uncommitted changes
2. Generate a changelog
3. Update the version in the project
4. Tag the release in git
5. Publish to Maven Central

