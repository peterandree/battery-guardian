# ADR-0003: Use periodic polling with adaptive intervals

- Status: Accepted
- Date: 2026-08-03

## Context

Battery APIs and device integrations frequently do not offer reliable push notifications. Even when events exist, they can be incomplete, delayed, unavailable after reconnects, or implemented differently across sources. Battery Guardian needs predictable evaluation without unnecessary CPU, Bluetooth, network, or battery use.

## Decision

Use periodic polling as the baseline acquisition mechanism.

Intervals are configurable and may be adaptive: use a normal interval while charge is safely inside a configured zone; use a shorter interval near an alert boundary or during rapid change; apply bounded exponential backoff after consecutive source failures; reset backoff after a successful read. Polling must be cancellable through the application lifecycle.

## Consequences

- Behaviour is consistent across sources with and without push support.
- Alert latency is bounded by the configured interval.
- Downstream evaluation must be idempotent because values may repeat.
- Reliable source-specific push signals may trigger immediate refreshes but do not replace periodic reconciliation.
