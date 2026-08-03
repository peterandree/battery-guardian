# ADR-0010: Dual Bluetooth Reading Methods

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Not all Bluetooth devices support the same battery reading method.

Inspiration from **BTChargeTrayWatcher** which implements both GATT and Classic reading.

## Decision

Implement **dual battery reading strategy**:
1. Prioritize GATT Battery Service
2. Fall back to Classic Bluetooth broadcasts
3. Cache device capabilities
4. Allow manual override

## Consequences

### Positive
- Broad Compatibility: Works with most devices
- Automatic Fallback: Tries multiple methods
- Performance: Caches successful methods
- User Experience: Minimal manual input
- Extensibility: Easy to add new methods
- Proven Pattern: Inspired by BTChargeTrayWatcher

### Negative
- Complexity: Multiple methods to maintain
- Testing: Need various devices
- Permissions: Different permissions needed

## Alternatives Considered
1. GATT Only: Misses Classic devices
2. Classic Only: Misses BLE devices
3. Manufacturer-Specific Only: Not scalable
4. Manual Input Only: Poor UX

## References
- [BTChargeTrayWatcher GattBatteryReader](https://github.com/peterandree/BTChargeTrayWatcher/blob/master/src/Monitoring/Gatt/GattBatteryReader.cs)
- [Bluetooth Battery Service](https://www.bluetooth.com/specifications/specs/battery-service-1-0/)