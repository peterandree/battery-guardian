# ADR-0009: Implement Hysteresis for Notifications

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Without hysteresis, users experience notification spam when battery levels oscillate around thresholds.

Hysteresis is used in **BTChargeTrayWatcher** (2% band).

## Decision

Implement **hysteresis with configurable dead band** (default: 2%) for alerts.

## Consequences

### Positive
- Better UX: Prevents notification spam
- Configurable: Users can adjust
- Simple: Easy to implement
- Proven: Used in BTChargeTrayWatcher

### Negative
- Slight Delay: Alerts clear later
- Complexity: Adds state management

## Alternatives Considered
1. Time-Based Cooldown: Less intuitive
2. Notification Grouping: Doesn't solve core problem
3. Rate Limiting: Too restrictive
4. No Hysteresis: Poor UX

## References
- [BTChargeTrayWatcher](https://github.com/peterandree/BTChargeTrayWatcher)
- [Hysteresis](https://en.wikipedia.org/wiki/Hysteresis)