# ADR-0001: Use a single process and explicit composition root

- Status: Accepted
- Date: 2026-08-03

## Context

Battery Guardian is a local monitoring application. Its initial scope does not require independent deployable services, a plugin host, or a general-purpose dependency-injection container. It still needs clear ownership of telemetry sources, state evaluation, persistence, notifications, and lifecycle handling.

## Decision

Use one application process and create long-lived application services in an explicit composition root.

Pass dependencies through constructors. Introduce interfaces at infrastructure boundaries and where test doubles are needed, not as a blanket rule.

## Consequences

- Startup and object ownership remain easy to understand.
- Unit tests can provide fake telemetry sources, clocks, stores, and notification sinks.
- The application avoids DI-container configuration and runtime resolution failures.
- Introduce a DI container only if composition becomes materially complex.
