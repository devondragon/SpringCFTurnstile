## [2.0.0] - 2025-12-29
# Changelog

## Features

### Spring Boot 4.0.x Compatibility and 2.0.0 Release Preparations
- The project has been updated for compatibility with Spring Boot 4.0.x, marking the release of version 2.0.0.
- Introduced a version compatibility matrix to the README to clearly document which library version to use based on Spring Boot version. Library version 2.0.x is for Spring Boot 4.0.x, considered the current version.
- Updated annotations for JUnit Jupiter to support version 6.0.x, as well as upgrades for Mockito with the introduction of the `@MockitoBean` annotation.
- Version numbers in the README for dependency examples were updated to reflect the new 2.0.0 release.
- Updated `gradle.properties` to version `2.0.0-SNAPSHOT`, preparing for the official 2.0.0 release.

## Fixes

### Improved Documentation of Configuration Properties
- Documentation improvement in the README to include all configuration properties for the Turnstile API, such as `url`, `connect-timeout`, `read-timeout`, and filter properties like `login.submissionPath`, `login.redirectUrl`, and `token.parameterName`. This ensures better guidance for users setting up the library.
- Changed the Java code example in the README from field injection to constructor injection using `@RequiredArgsConstructor`, following best practices and promoting better code maintainability.

## Breaking Changes

### Spring Boot 4.0.x Actuator Modularization
- As part of the Spring Boot 4.0 upgrade, several important API endpoints and packages have been reorganized due to modularization efforts:
  - `ConditionalOnEnabledHealthIndicator` moved to `org.springframework.boot.health.autoconfigure.contributor`.
  - `MeterRegistryCustomizer` moved to `org.springframework.boot.micrometer.metrics.autoconfigure`.
  - `Health` and `HealthIndicator` moved to `org.springframework.boot.health.contributor`.
- Developers need to update their import statements to these new package paths.

## Documentation

### Comprehensive Update for 2.0.0 Release
- Added a comprehensive entry for the 2.0.0 release in the `CHANGELOG.md`, and a badge for Spring Boot 4.0.x in the README header.
- Added a version compatibility table for clarity regarding which library version corresponds with which Spring Boot version, reducing confusion for developers.

## Other Changes

### Dependency Bumps
- Updated `org.junit.jupiter:junit-jupiter` from 6.0.0 to 6.0.1 for improved test stability and features.
- Upgraded the Spring Boot dependency from version 3.5.7 to 4.0.1, marking this a major release improvement.
- Updated `com.vanniktech.maven.publish` plugin from 0.34.0 to 0.35.0, ensuring better publication processes and alignment with the latest tooling features.

### Development Tools and Configuration:
- Updated Claude configuration in `.claude/settings.local.json` to include additional Bash scripts and fetch capabilities, enabling enhanced integration and operations for the development environment.

These changes illustrate a major upgrade and compatibility overhaul, focusing on long-term maintainability, documentation clarity, and support for modern Spring Boot environments. Developers should thoroughly review the breaking changes, especially around Spring Boot's modularization, to align their projects with these updates.

## [2.0.0] - 2025-12-29
### Changelog

#### Breaking Changes
- **Spring Boot 4.0.x Upgrade**
  - This release requires Spring Boot 4.0.0 or later
  - Users on Spring Boot 3.5.x should use version 1.3.x (maintenance branch: `spring-boot-3.x`)

#### Features
- **Spring Boot 4.0 Compatibility**
  - Updated actuator health indicator imports to `org.springframework.boot.health.contributor` package
  - Full compatibility with Jackson 3.x (core annotations remain backward compatible)
  - JUnit Jupiter 6.0.x test framework support
  - Updated to `@MockitoBean` annotation (replacing deprecated `@MockBean`)

#### Documentation
- **Version Compatibility Matrix**
  - Added clear version compatibility table to README
  - Documents which library version to use based on Spring Boot version
  - Updated dependency examples to version 2.0.0

#### Other Changes
- **Maintenance Branch Created**
  - Created `spring-boot-3.x` branch for Spring Boot 3.5.x users
  - Tagged v1.3.0 as the final Spring Boot 3.5.x release

---

## [1.2.0] - 2025-09-16
### Changelog

#### Features
- **Enhanced Turnstile Service Configuration and Validation** (Commit: 91743a8e)
  - **TurnstileServiceConfig:** Added support for `MeterRegistry` to allow better integration with metrics tracking systems.
  - **TurnstileValidationService:** Improved IP address extraction by utilizing a more extensive and robust header check.
  - **TurnstileResponse:** Introduced `@JsonProperty("challenge_ts")` for direct JSON mapping of challenge timestamps.
  - **turnstile.properties:** Updated comments for clearer configuration of `sitekey` and `secret`, advising environment customization.

#### Fixes
- **Turnstile Service Configuration and Dependency Updates** (Commit: 886d12bf)
  - Removed unnecessary `RestTemplate` setup from `TurnstileServiceConfig`.
  - Enhanced logging in `TurnstileValidationService` to confirm secret configuration status.
  - Updated `README.md` to reflect new dependency versions, ensuring up-to-date integration instructions.
  - Refined IP validation, cleaning remote IP assignments, ensuring null or empty strings are managed correctly.
  - Adjusted code and style rules to maintain code quality consistency.

#### Breaking Changes
- **Compatibility with Vanniktech Maven Publish Plugin 0.34.0** (Commit: 1aaefea4)
  - Deprecated usage of `SonatypeHost` removed in favor of configuration via `gradle.properties`.
  - **gradle.properties:** Introduced `mavenCentralPublishing` and `mavenCentralAutomaticPublishing` properties to align with new plugin expectations.

#### Documentation
- **AI Agent Setup and Project Guidelines** (Commit: ad46b8ad)
  - Added `.claude/settings.local.json` to manage AI agent permissions and actions within the repository.
  - Created `AGENTS.md` with detailed guidance on project structure, coding standards, testing practices, and repository guidelines to support contributors.

#### Other Changes
- **Dependency Updates:**
  - Automated updates of `org.junit.jupiter:junit-jupiter` from versions 5.13.1 to 5.13.4, ensuring developers have access to the latest JUnit features and fixes (Commits: fabff8fe, 63709833, 4bb4ba1f).
  - Upgraded `org.projectlombok:lombok` to 1.18.40 for potential improvements in Lombok's annotation processing (Commit: 4742a9e1).

This changelog captures significant improvements to Turnstile service validation, introduces necessary configuration updates for the Maven publish plugin compatibility, adds helpful documentation for repository maintenance and usage, all while keeping dependencies current and addressing minor bug fixes.

## [1.1.9] - 2025-06-23
## Changelog

### Features
- **Enhance testing configuration**: Added annotation processor ``org.springframework.boot:spring-boot-configuration-processor`` for improved processing of Spring Boot configurations during tests. Additionally, ensured the `useJUnitPlatform()` configuration for better JUnit 5 support and updated test dependencies to enhance testing capabilities. [Commit 7789dbee]

- **CI/CD and code quality tools**: Implemented GitHub Actions workflows to support automated builds, testing, and quality checks. These include workflows for building and testing on multiple Java versions (Java 17 and 21), dependency review for security vulnerabilities, and CodeQL for security analysis. Integrated code quality tools using Gradle plugins: Checkstyle for code style enforcement, PMD for detecting code issues, and JaCoCo for code coverage analysis. [Commit 8115c34d]

- **Create `codeql.yml`**: Added a new GitHub Actions workflow to perform CodeQL analysis on pushes, pull requests, and weekly schedules, strengthening the project's security posture. [Commit ea13cfd0]

### Fixes
- **Turnstile service enhancement**: Improved Turnstile service by implementing new test cases, updating the PMD and Checkstyle configurations, and refactoring validation service tests for improved clarity. Included JUnit 5 dependencies to enhance the testing framework and updated Gradle wrapper to version 8.14.1 for improvements in build system robustness. [Commit b089d625]

### Breaking Changes
- None identified in the provided information.

### Refactoring
- None explicitly identified, but substantial test refactoring and service enhancements were addressed as part of the Turnstile service improvement.

### Documentation
- **Version Update in README**: Updated README.md to reflect the new version 1.1.8 of the `ds-spring-cf-turnstile` library to ensure users refer to the latest library version. Added Maven Central, License, and Java Version badges for better informational display. [Commits 8b36ae21, 048cd847]

### Testing
- **Enhanced Test Configurations**: Added Lombok annotation processor specifically for test compilation to facilitate comprehensive test generation and execution. Implemented the `useJUnitPlatform()` setting to utilize JUnit 5 effectively within the testing suite. [Commit 7789dbee]

### Other Changes
- **Dependency Upgrades**: Executed multiple dependency upgrades for improved compatibility and newer features, including upgrading `org.junit.jupiter:junit-jupiter` from 5.12.2 to 5.13.1, and `com.vanniktech.maven.publish` from 0.31.0 to 0.33.0. Spring Boot version was incrementally updated through versions 3.4.3 to 3.5.3 for enhanced security and features. [Commits 29bd16ab, 778c68b7, 0d5ae342, 0b12e213]

- **Gradle Wrapper Upgrades**: Gradle wrapper versions were updated through several updates from 8.12.1 to 8.14.1 for enhanced features and performance in builds. [Commits 3d337b0e, 3deb5006, b089d625]

This changelog captures the essence of substantial changes, improvements, and critical updates to the overall project infrastructure, dependencies, testing, and code quality tools. The focus remains on explaining the pivotal changes in user-centric terms and highlighting updates that directly impact end-users and developers interacting with the repository.

## [1.1.8] - 2025-03-13
# Changelog

## Features

### Added CLAUDE.md with Build Commands and Code Style Guidelines
- A new `CLAUDE.md` file was created to document build commands and code style guidelines.
- Provides commands for building, testing on specific JDK versions, running single tests, and publishing locally.
- Establishes Java code style guidelines, including Java 17+ compatibility, Lombok annotations usage, documentation requirements, and package naming conventions.
- Specifies best practices for dependency injection, exception handling, and consistent code formatting. ([commit 5de32522](https://github.com/devondragon/SpringCFTurnstile/commit/5de32522))

### Added Turnstile Captcha Validation Filter and Configuration
- Introduced `TurnstileCaptchaFilter` to validate captcha tokens during Spring Security form login submissions.
- Configuration properties allow customization of login submission paths, redirect URLs, and token parameter names.
- Provides integration instructions with Spring Security Setup for streamlined captcha validation. ([commits 29db7fba](https://github.com/devondragon/SpringCFTurnstile/commit/29db7fba))

### Added Monitoring and Metrics Support
- Implemented Spring Actuator metrics for validation counts, success/failure rates, and response times.
- Added health check to monitor Cloudflare connectivity, configurable via the application properties.
- Supports Micrometer metrics for integration with monitoring systems like Prometheus and Grafana. ([commits 24583689, 57516314](https://github.com/devondragon/SpringCFTurnstile/commit/24583689))

## Fixes

### Improved Error Handling and Resilience
- Added multiple custom exception classes for various error scenarios.
- Introduced `ValidationResult` class for detailed validation error reporting.
- Enhanced `TurnstileValidationService` to categorize and handle different error types, maintaining backward compatibility. ([commits 2a45c40e, 306bc141](https://github.com/devondragon/SpringCFTurnstile/commit/2a45c40e))

### Fixed Method Name Typo: `getTurnsiteSitekey` to `getTurnstileSitekey`
- Corrected a method name typo in `TurnstileValidationService` for improved code clarity and usage.
- Deprecated the old method while ensuring backward compatibility. ([commits e0880f76, f03e68bb](https://github.com/devondragon/SpringCFTurnstile/commit/e0880f76))

## Documentation

### Improved Documentation Consistency and Completeness
- Created `CONTRIBUTING.md` with guidelines for contributors.
- Enhanced existing documentation with additional security best practices and architecture overview.
- Fixed and updated the license information in the `README` to Apache 2.0 and added security best practices. ([commits 7e79b31f, 64199aa4](https://github.com/devondragon/SpringCFTurnstile/commit/7e79b31f))

### Added License Files
- Introduced comprehensive `LICENSE.md` and `LICENSE.txt` files conveying Apache License 2.0 terms. ([commits 7e79b31f, 1e166f07](https://github.com/devondragon/SpringCFTurnstile/commit/7e79b31f))

## Testing

### Added and Updated Tests for Turnstile Validation
- Enhanced tests for `TurnstileValidationService`, ensuring coverage for new error handling and input validation scenarios.
- Confirmed validation logic processes null, empty, and short tokens as well as remote IP address issues. ([commits 2ac832ac](https://github.com/devondragon/SpringCFTurnstile/commit/2ac832ac))

## Other Changes

### Dependency Updates
- Bumped `springBootVersion` to 3.4.3 and updated `com.vanniktech.maven.publish` and `com.github.ben-manes.versions` plugins versions for better compatibility and functionality. ([commits 494ee82c, 63dc315e](https://github.com/devondragon/SpringCFTurnstile/commit/494ee82c))
- Updated Gradle wrapper properties to version 8.12.1 for overall build tool enhancements. ([commit 6ba0ef13](https://github.com/devondragon/SpringCFTurnstile/commit/6ba0ef13))

---

This changelog focuses on summarizing significant feature additions, fixes, and other enhancements for easy reference by developers and users alike. For detailed code reviews, refer to individual commit messages and linked pull requests.

## [1.1.7] - 2024-12-31
Based on the commit messages, the changelog would be:

### Features
- Updated to version 1.1.7.
- Dependencies for post-release tasks added.

### Fixes
- Commented out the dependency on publishReposilite in the afterReleaseBuild task to prevent issues during the build.

### Breaking Changes
- None in this release. There were no breaking changes based on the commit messages provided.

## [1.1.7] - 2024-12-31
### Features
- Gradle Release Plugin has been updated to a new version: '1.1.7-SNAPSHOT'.

### Fixes
- Updated build.gradle by commenting out the version and adding dependencies necessary for post-release tasks.

### Breaking Changes
- PublishReposilite dependency in task "afterReleaseBuild" has been commented out, which may disrupt build processes depending on it.

## [1.1.6-SNAPSHOT] - 2024-12-31
Based on the commit messages, here is the changelog:

### Features
- None

### Fixes
- Updated version in gradle.properties for consistency
- Bump version to 1.1.6 in gradle.properties


### Breaking Changes
- None

## [1.1.6-SNAPSHOT] - 2024-12-31
### Features
- Added versioning information in `gradle.properties`.
- Added Spring Boot web starter as a test implementation dependency.
- Changelog generation script added, along with an update in build configuration for publishing.

### Fixes
- Fixed Python command in changelog generation task for better compatibility.
- Updated changelog generation command now uses 'mise' for better compatibility.
- Dependency management plugin and Spring Boot version updated, changed implementation to `compileOnly` for web starter.

### Breaking Changes
- Bumped org.projectlombok:lombok from version 1.18.34 to 1.18.36.
- Bumped org.springframework.boot from version 3.3.4 to version 3.3.5, and then to version 3.4.0. This substantial version update may cause breaking changes.
- README document was updated, which may affect users who refer to it for usage or installation instructions.

