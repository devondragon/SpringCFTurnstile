# Contributing to Spring Cloudflare Turnstile

Thank you for considering contributing to Spring Cloudflare Turnstile! This document outlines the process for contributing to the project and provides guidelines to follow.

## Code of Conduct

By participating in this project, you agree to be respectful to all contributors regardless of gender, gender identity, sexual orientation, disability, age, race, ethnicity, religion, or level of experience.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue in the [issue tracker](https://github.com/devondragon/SpringCFTurnstile/issues) with the following information:

1. A clear, descriptive title
2. Steps to reproduce the issue
3. Expected behavior
4. Actual behavior
5. Any relevant logs or stack traces
6. Your environment (Java version, Spring Boot version, etc.)

### Feature Requests

Feature requests are welcome. Please create an issue in the [issue tracker](https://github.com/devondragon/SpringCFTurnstile/issues) with:

1. A clear, descriptive title
2. A detailed description of the feature you'd like to see
3. Any relevant examples or use cases
4. Why this feature would be beneficial to the project

### Pull Requests

1. Fork the repository
2. Create a new branch: `git checkout -b my-feature-branch`
3. Make your changes
4. Add tests for your changes
5. Run the test suite to ensure all tests pass: `./gradlew test`
6. Update documentation if necessary
7. Commit your changes using descriptive commit messages
8. Push to your fork: `git push origin my-feature-branch`
9. Create a pull request against the `main` branch

## Development Environment Setup

1. Clone the repository: `git clone https://github.com/devondragon/SpringCFTurnstile.git`
2. Navigate to the project directory: `cd SpringCFTurnstile`
3. Build the project: `./gradlew build`

## Testing Guidelines

- All code changes should include tests
- Tests should cover both success and failure scenarios
- Try to maintain or improve code coverage

## Code Style Guidelines

- Follow the existing code style in the project
- Use clear, descriptive variable and method names
- Add comprehensive JavaDoc comments for all public methods and classes
- Keep methods focused and reasonably sized

## Versioning

This project follows [Semantic Versioning](https://semver.org/). In summary:

- MAJOR version for incompatible API changes
- MINOR version for functionality added in a backward-compatible manner
- PATCH version for backward-compatible bug fixes

## License

By contributing to this project, you agree that your contributions will be licensed under the project's [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).