# ADR-0006: Use Jetpack Compose for UI

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian requires a modern, responsive UI.

Jetpack Compose is the **recommended** UI toolkit for new Android apps and is used in **Workday-Wake**.

## Decision

Use **Jetpack Compose** for all UI.

## Consequences

### Positive
- Declarative: UI as function of state
- Concise: Less code than XML
- Type Safety: Compile-time checking
- Reusability: Composable functions
- Testability: Easy to test
- Performance: Efficient recomposition
- Modern: Industry-standard
- Consistency: Aligns with Workday-Wake

### Negative
- Learning Curve: New concepts
- Tooling: Limited design tool support
- Migration: Harder to migrate

## Alternatives Considered
1. XML + Views: Verbose, harder to maintain
2. Flutter/React Native: Not native Android
3. Hybrid Approach: Adds complexity

## References
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Workday-Wake UI](https://github.com/peterandree/Workday-Wake/tree/master/app/src/main/kotlin/com/workdaywake/ui)