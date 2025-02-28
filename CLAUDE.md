# SpringCFTurnstile Build Guide

## Build Commands
- Full build: `gradle build`
- Run all tests: `gradle test`
- Test on specific JDK: `gradle testJdk17` or `gradle testJdk21`
- Run single test: `gradle test --tests "com.digitalsanctuary.cf.test.turnstile.TurnstileValidationServiceTest"`
- Publish locally: `gradle publishToMavenLocal`

## Code Style Guidelines
- Java 17+ compatibility required
- Use Lombok annotations (@Data, @Slf4j, @RequiredArgsConstructor)
- Always document public methods and classes with JavaDoc
- Follow Spring Boot patterns for configuration and service classes
- Use RestClient for HTTP communication
- Consistent package naming: com.digitalsanctuary.cf.turnstile.*
- Prefer constructor injection over field injection
- Handle nulls through Optional rather than null checks
- Use proper exception handling with meaningful messages
- Maintain consistent 4-space indentation
- Follow standard Java naming conventions (camelCase methods/variables, PascalCase classes)