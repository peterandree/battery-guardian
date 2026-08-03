# Contributing to Battery Guardian

Thank you for your interest in contributing to Battery Guardian! This document outlines how to contribute to the project.

---

## Getting Started

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/battery-guardian.git
   cd battery-guardian
   ```
3. **Set up the project** in Android Studio:
   - Open the project in Android Studio.
   - Ensure you have the required SDK versions installed (minSdk 31, targetSdk 35).
   - Sync Gradle dependencies.

4. **Run the app**:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Development Workflow

1. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```
   Branch naming conventions:
   - `feature/` for new features
   - `fix/` for bug fixes
   - `chore/` for maintenance tasks
   - `docs/` for documentation updates

2. **Make your changes** following the coding standards in `AGENTS.md`.

3. **Run tests and lint**:
   ```bash
   ./gradlew lint test
   ```

4. **Commit your changes** with a descriptive message following the commit format in `AGENTS.md`.

5. **Push to your fork** and create a pull request to the `master` branch of the main repository.

---

## Pull Request Guidelines

- Follow the pull request template provided in `.github/pull_request_template.md`.
- Ensure all tests pass and lint is clean.
- Reference the issue your PR addresses with `Closes #issue-number`.
- Include screenshots or videos for UI changes.
- Keep PRs focused and limited in scope.

---

## Code Review Process

- All PRs require at least one approval from a maintainer.
- Address all review comments before merging.
- Update the PR description if changes are made during review.

---

## Reporting Issues

When reporting issues, please include:
- Android version
- Device model
- Bluetooth device models
- Steps to reproduce
- Expected vs. actual behavior
- Relevant logs or screenshots

---

## License

By contributing to Battery Guardian, you agree that your contributions will be licensed under the MIT License.