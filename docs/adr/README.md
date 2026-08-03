# Architectural Decision Records

This directory contains Architectural Decision Records (ADRs) for Battery Guardian. ADRs document significant architectural decisions along with their context, trade-offs, and consequences.

---

## What is an ADR?

An Architectural Decision Record (ADR) is a document that captures an important architectural decision made along with its context and consequences. ADRs help track the "why" behind architectural choices and provide a way to revisit decisions when circumstances change.

---

## ADR Format

Each ADR follows this structure:

```markdown
# ADR-000: Title

- **Status:** Accepted / Proposed / Rejected / Deprecated / Superseded
- **Date:** YYYY-MM-DD
- **Author:** @username

## Context

The issue or problem being addressed. What forces are at play?

## Decision

The change being proposed or implemented. What is the solution?

## Consequences

What becomes easier or more difficult to do because of this change?

## Alternatives Considered

What other options were considered? Why were they rejected?

## References

Links to relevant documents, issues, or external resources.
```

---

## ADR Index

| Number | Title | Status | Date | Author |
|--------|-------|--------|------|--------|
| [ADR-0001](0001-layered-architecture.md) | Adopt Layered Architecture | Accepted | 2026-08-03 | @peterandree |
| [ADR-0002](0002-kotlin-and-coroutines.md) | Use Kotlin with Coroutines for Async | Accepted | 2026-08-03 | @peterandree |
| [ADR-0003](0003-room-database.md) | Use Room for Local Data Persistence | Accepted | 2026-08-03 | @peterandree |
| [ADR-0004](0004-datastore-preferences.md) | Use DataStore for User Preferences | Accepted | 2026-08-03 | @peterandree |
| [ADR-0005](0005-hilt-dependency-injection.md) | Use Hilt for Dependency Injection | Accepted | 2026-08-03 | @peterandree |
| [ADR-0006](0006-jetpack-compose.md) | Use Jetpack Compose for UI | Accepted | 2026-08-03 | @peterandree |
| [ADR-0007](0007-background-operation.md) | Background Operation Strategy | Accepted | 2026-08-03 | @peterandree |
| [ADR-0008](0008-battery-prediction.md) | Use Linear Regression for Battery Prediction | Accepted | 2026-08-03 | @peterandree |
| [ADR-0009](0009-hysteresis-notifications.md) | Implement Hysteresis for Notifications | Accepted | 2026-08-03 | @peterandree |
| [ADR-0010](0010-dual-bluetooth-reading.md) | Dual Bluetooth Reading Methods | Accepted | 2026-08-03 | @peterandree |

---

## How to Add a New ADR

1. Create a new file in this directory named `NNNN-description.md` where `NNNN` is the next available number
2. Follow the ADR format template above
3. Add the ADR to the index table above
4. Commit the changes with a message like `docs(adr): add ADR-0011 for new feature`

---

## ADR Status Definitions

- **Accepted:** The decision has been implemented
- **Proposed:** The decision is under consideration
- **Rejected:** The decision was considered but not implemented
- **Deprecated:** The decision was implemented but is no longer relevant
- **Superseded:** The decision has been replaced by a newer decision

---

## Inspiration

ADRs in this project are inspired by:
- [Workday-Wake ADRs](https://github.com/peterandree/Workday-Wake/tree/master/docs/adr)
- [BTChargeTrayWatcher architecture](https://github.com/peterandree/BTChargeTrayWatcher)
- [MADR (Markdown Architectural Decision Records)](https://adr.github.io/madr/)
