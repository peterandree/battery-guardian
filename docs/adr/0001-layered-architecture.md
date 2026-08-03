# ADR-0001: Adopt Layered Architecture

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian needs to monitor Bluetooth device batteries, predict drain rates, and alert users. The application must be:
- **Maintainable:** Easy to understand, modify, and extend
- **Testable:** Components can be tested in isolation
- **Scalable:** Can grow without becoming unwieldy
- **Reliable:** Functions correctly across different Android versions and devices

Without a clear architecture, the codebase risks becoming a "big ball of mud" where:
- Business logic is mixed with UI code
- Data access is scattered throughout the app
- Changes in one area break unrelated functionality
- Testing requires complex mocking and setup

This pattern is successfully used in **Workday-Wake** and provides a proven foundation for Android apps with similar requirements (scheduling, notifications, data persistence).

## Decision

Adopt a **layered architecture** with the following layers and dependencies:

```
┌─────────────────────────────────────┐
│           UI Layer                   │
│  (Jetpack Compose, ViewModels)      │
├─────────────────────────────────────┤
│           Domain Layer               │
│  (Use Cases, Models, Interfaces)    │
├─────────────────────────────────────┤
│           Data Layer                 │
│  (Repositories, Room, DataStore)     │
├─────────────────────────────────────┤
│         Platform Layer               │
│  (Bluetooth, Services, Receivers)   │
└─────────────────────────────────────┘
```

**Layer Rules:**

1. **UI Layer**
   - Contains: Screens, ViewModels, Composable Functions, Theme, Navigation
   - Depends on: Domain Layer
   - Does NOT depend on: Data Layer or Platform Layer
   - Technologies: Jetpack Compose, ViewModel, Navigation Component

2. **Domain Layer**
   - Contains: Use Cases, Domain Models, Repository Interfaces
   - Depends on: Nothing (pure Kotlin)
   - Does NOT depend on: Any Android framework classes
   - Technologies: Pure Kotlin, Coroutines

3. **Data Layer**
   - Contains: Repository Implementations, Room Database, DataStore, Bluetooth Data Sources
   - Depends on: Domain Layer (interfaces), Platform Layer (Android APIs)
   - Does NOT depend on: UI Layer
   - Technologies: Room, DataStore, Android Bluetooth API

4. **Platform Layer**
   - Contains: Bluetooth API Wrappers, Foreground Service, Broadcast Receivers, AlarmManager/WorkManager
   - Depends on: Data Layer (for persistence)
   - Does NOT depend on: UI Layer
   - Technologies: Android Bluetooth API, Services, Receivers, AlarmManager

## Consequences

### Positive

1. **Separation of Concerns:** Each layer has a single, well-defined responsibility
2. **Testability:** Domain layer can be tested without Android runtime; UI can be tested with mock ViewModels
3. **Maintainability:** Changes in one layer have minimal impact on others
4. **Flexibility:** Can swap out implementations (e.g., change database) without affecting business logic
5. **Reusability:** Domain models and use cases can be reused across different UI platforms
6. **Consistency:** Follows established patterns from Workday-Wake

### Negative

1. **Boilerplate:** Requires creating interfaces and implementations for repositories
2. **Learning Curve:** Developers need to understand the layering rules
3. **Navigation:** Requires careful navigation between layers
4. **Build Time:** Additional modules may increase build time slightly

### Neutral

1. **File Count:** More files due to separation, but each file is smaller and more focused

## Alternatives Considered

### 1. MVVM Only
- **Rejected:** Doesn't provide enough separation; business logic ends up in ViewModels
- **Issue:** Harder to test, harder to maintain

### 2. Clean Architecture (Hexagonal)
- **Rejected:** More complex than needed for this project
- **Issue:** Over-engineering for current requirements
- **Note:** Could evolve to this if complexity grows

### 3. Feature Modules
- **Rejected:** Premature for current scope
- **Issue:** Adds complexity without clear benefit
- **Note:** Can be introduced later if app grows significantly

### 4. Flat Architecture
- **Rejected:** Leads to spaghetti code
- **Issue:** No separation of concerns, hard to maintain

## References

- [Workday-Wake Architecture](https://github.com/peterandree/Workday-Wake/blob/master/docs/architecture.md)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
