# ADR-0009: Keep telemetry local by default and redact diagnostics

- Status: Accepted
- Date: 2026-08-03

## Context

Battery telemetry can expose device names, hardware identifiers, usage patterns, network endpoints, and notification configuration. The application’s core purpose does not require cloud collection.

## Decision

Keep configuration, current state, alert history, and diagnostics on the local device by default. Do not transmit telemetry automatically.

Diagnostic logging uses structured events and redacts secrets, remote notification URLs, tokens, and user-defined aliases when exported. Data export and remote forwarding require an explicit user action or opt-in setting.

## Consequences

- Default operation has minimal privacy and compliance surface.
- Diagnostics remain useful without disclosing credentials.
- Synchronization, analytics, and cloud dashboards become explicit future architecture decisions rather than incidental behaviour.
