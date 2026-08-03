# ADR-0002: Isolate battery telemetry behind source adapters

- Status: Accepted
- Date: 2026-08-03

## Context

Battery data may come from operating-system APIs, Bluetooth peripherals, vendor tools, networked sources, or future integrations. These sources differ in availability, accuracy, update frequency, and supported fields. Domain logic must not depend on a particular API or device protocol.

## Decision

Represent every telemetry integration behind a `BatteryTelemetrySource` abstraction that returns normalized snapshots.

A snapshot contains at least a stable source or device ID, observed timestamp, charge percentage when available, charging state when available, source health or read failure information, and optional metadata such as manufacturer, model, and display name. Adapters own source-specific parsing, validation, and error mapping; the domain evaluates normalized snapshots only.

## Consequences

- New sources can be added without changing alert and policy logic.
- Tests can use deterministic fake sources.
- Source limitations remain visible through capability and health metadata.
- Normalization requires deliberate handling of missing or conflicting measurements.
