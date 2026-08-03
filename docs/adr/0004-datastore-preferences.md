# ADR-0004: Use DataStore for User Preferences

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian needs to store user preferences with type safety and reactivity.

DataStore is the **recommended** solution for modern Android apps and is used in **Workday-Wake**.

## Decision

Use **DataStore Preferences** for all user preferences.

## Consequences

### Positive
- Type Safety: Compile-time type checking
- Reactivity: Automatic UI updates
- Simplicity: Clean API
- Consistency: Single source of truth
- Performance: Asynchronous I/O
- Consistency: Aligns with Workday-Wake

### Negative
- Learning Curve: Different from SharedPreferences
- Migration: Requires migration from SharedPreferences

## Alternatives Considered
1. SharedPreferences: No type safety, no reactivity
2. Custom File Storage: More work
3. Room Database: Overkill for key-value pairs

## References
- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Workday-Wake UserPreferencesRepository](https://github.com/peterandree/Workday-Wake/blob/master/app/src/main/kotlin/com/workdaywake/data/preferences/UserPreferencesRepository.kt)