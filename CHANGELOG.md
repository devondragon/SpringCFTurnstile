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

