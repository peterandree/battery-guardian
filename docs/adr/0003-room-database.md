# ADR-0003: Use Room for Local Data Persistence

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian needs to persist several types of data:
- Device information (name, type, capabilities, etc.)
- Battery level history
- Battery health metrics
- Alert states and thresholds

Requirements:
- **Reliability:** Data must not be lost
- **Performance:** Queries must be fast
- **Simplicity:** Easy to use and maintain
- **Type Safety:** Compile-time checking
- **Schema Evolution:** Ability to change database schema
- **Offline-First:** All data stored locally

Room is the **recommended** persistence library for Android and is used in **Workday-Wake**.

## Decision

Use **Room** for all local data persistence.

## Consequences

### Positive
- Type Safety: Compile-time checking of SQL queries
- Simplicity: Reduces boilerplate code
- Performance: Optimized for Android
- Maintainability: Clear separation
- Schema Management: Handles migrations
- Consistency: Aligns with Workday-Wake
- Google Support: Officially recommended

### Negative
- Learning Curve: Requires understanding Room annotations
- Build Time: Schema export adds overhead
- Complexity: More complex than SharedPreferences

## Alternatives Considered
1. SQLite Directly: No compile-time checking
2. Realm: Proprietary license
3. ObjectBox: Less mature
4. SharedPreferences Only: Not suitable for complex data

## References
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Workday-Wake Room Usage](https://github.com/peterandree/Workday-Wake/tree/master/app/src/main/kotlin/com/workdaywake/data/local)