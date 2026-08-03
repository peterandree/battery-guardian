# ADR-0007: Use local-first notification delivery behind an outbound adapter

- Status: Accepted
- Date: 2026-08-03

## Context

Battery alerts are most valuable when visible on the host device. Some users may also want mobile or remote notifications, but external services can be unavailable and must not compromise core monitoring.

## Decision

Define a `NotificationSink` boundary and use local operating-system notifications as the default and required sink.

Remote channels, such as ntfy or webhooks, are optional sinks. A remote delivery failure is recorded as a non-fatal diagnostic event and must not block local notification delivery or telemetry polling. User-facing content uses the display name or alias; stable source IDs are retained internally.

## Consequences

- The core application works without accounts, network connectivity, or a hosted service.
- Remote delivery can be added, disabled, and tested independently.
- Multiple sinks may create duplicate user-visible alerts; this must be an explicit configuration choice.
- Credentials and private endpoints require secure storage and redacted logging.
