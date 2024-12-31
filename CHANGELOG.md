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

