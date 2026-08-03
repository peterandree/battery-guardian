# ADR-0005: Use threshold zones, hysteresis, and alert cooldowns

- Status: Accepted
- Date: 2026-08-03

## Context

Battery percentages fluctuate and may have limited precision. Alerting exactly at a single threshold can repeatedly notify the user while a value oscillates around it. The user needs actionable alerts rather than a stream of near-duplicates.

## Decision

Model alerting as transitions between threshold zones rather than independent comparisons on every sample.

Each policy defines an enter threshold, an exit threshold for hysteresis, an optional minimum cooldown, a state condition such as charging or discharging, and notification severity. Emit an alert when a source enters a zone; re-arm only after it exits through the exit threshold or after a separately configured reminder interval.

## Consequences

- Boundary flapping does not create notification spam.
- Policies can express “notify below 20%, re-arm above 25%”.
- Evaluation requires persisted per-source alert state.
- Threshold changes must safely re-evaluate and reconcile existing state.
