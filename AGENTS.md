# Repository Guidelines

## Project Structure & Module Organization
Runtime code lives in `src/main/java/com/digitalsanctuary/cf/turnstile`, grouped by concern (`config`, `service`, `filter`, `dto`, `exception`). Shared configuration assets sit in `src/main/resources`, including `config/turnstile.properties` and Spring metadata under `META-INF`. Tests mirror the package layout in `src/test/java/com/digitalsanctuary/cf/test/turnstile`, while test fixtures and templates belong in `src/test/resources`. Build outputs flow to `build/`, and quality configurations are kept under `config/` (`checkstyle`, `pmd`).

## Build, Test, and Development Commands
Use `./gradlew clean build` for a full verification and artifact assembly. `./gradlew test` launches the JUnit suite; add `-Porg.gradle.java.installations.auto-download=false` when relying on preinstalled JDKs. `./gradlew testAll` exercises the matrix on Java 17 and 21, mirroring CI. Run `./gradlew jacocoTestReport` to generate coverage reports in `build/reports/jacoco/test/html/index.html`. Publishing helpers exist for maintainers: `./gradlew publishLocal` installs to the local Maven cache, and `./gradlew publishReposilite` targets the private staging host.

## Coding Style & Naming Conventions
Target Java 17+ and favor Lombok for boilerplate already in use. Follow Checkstyle guidance (`config/checkstyle/checkstyle.xml`): four-space indentation, braces on the same line, and public APIs documented with Javadoc. Keep packages lowercase, classes `PascalCase`, and fields/methods `camelCase`. PMD rules in `config/pmd/ruleset.xml` flag unused complexity; resolve warnings rather than suppressing unless justified.

## Testing Guidelines
Tests rely on JUnit Jupiter with Spring’s test utilities. Name classes with the `*Test` suffix (e.g., `TurnstileValidationServiceTest`) and focus on observable behavior around validation, filters, and error handling. Run `./gradlew test` before every push, and refresh coverage via `./gradlew jacocoTestReport`; aim to keep line coverage above the current 48% threshold noted in the Gradle configuration even though enforcement is disabled.

## Commit & Pull Request Guidelines
Write imperative, present-tense commit titles ("Add Turnstile captcha filter") and include one change per commit when practical. Reference issues or Dependabot tickets with `Fixes #<id>` in the body when closing them. Pull requests should summarize intent, list verification commands (Gradle tasks, coverage), and attach screenshots or logs when touching user-facing responses. Keep branches current with `main` and request review once tests pass.

## Configuration & Security Tips
Store site keys and secrets outside version control; use environment overrides or Spring Boot configuration properties. Validate Cloudflare connectivity against non-production keys before promoting changes, and redact sensitive responses from logs when updating filters or exception handlers.
