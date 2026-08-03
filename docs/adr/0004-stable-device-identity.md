# ADR-0004: Use stable source identity separately from display names

- Status: Accepted
- Date: 2026-08-03

## Context

Users may rename devices or sources. Display names can also change after reconnecting, firmware upgrades, localization changes, or adapter improvements. Settings, alert history, and state transitions must continue to apply to the same physical or logical source.

## Decision

Persist and correlate monitored batteries by a stable, source-qualified identifier in the form `<source-kind>:<stable-source-id>`.

Store the user-facing display name as mutable metadata. Allow a local alias without changing the stable identity.

## Consequences

- Device-specific policies survive display-name changes.
- Notifications and diagnostics remain human-readable.
- Identifier-format changes require explicit migrations.
- Weak source identifiers require documented collision and migration behaviour.
