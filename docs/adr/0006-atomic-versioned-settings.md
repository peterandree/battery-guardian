# ADR-0006: Persist settings atomically and version their schema

- Status: Accepted
- Date: 2026-08-03

## Context

Configuration controls monitoring intervals, source selection, threshold policies, notification routing, and user-visible names. Partial writes or incompatible upgrades could silently disable alerting or corrupt configuration.

## Decision

Store configuration in a single versioned settings document and replace it atomically.

Validate a complete candidate settings object before it becomes active. Keep domain validation separate from serialization and platform storage concerns. Implement explicit migrations from older schema versions. If recovery is required, preserve the invalid file for diagnostics and start with safe defaults rather than silently overwriting it.

## Consequences

- Readers observe either the old valid configuration or the new valid configuration.
- Settings changes can be tested as pure validation and migration operations.
- Future schema changes have a defined compatibility path.
- Atomic replacement behaviour must be implemented for the target platform.
