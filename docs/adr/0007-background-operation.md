# ADR-0007: Background Operation Strategy

- **Status:** Accepted
- **Date:** 2026-08-03
- **Author:** @peterandree

## Context

Battery Guardian must monitor Bluetooth device batteries continuously, even when app is in background or device is in Doze Mode.

Android imposes strict restrictions on background operations.

Inspiration from **Workday-Wake** and **BTChargeTrayWatcher**.

## Decision

Use multi-layered strategy:
1. Foreground Service for continuous monitoring
2. AlarmManager for critical alerts
3. WorkManager for periodic polling
4. BootCompletedReceiver for restart after reboot
5. Battery Optimization Exemption

## Consequences

### Positive
- Reliability: Multiple mechanisms
- Battery Efficiency: WorkManager respects optimizations
- User Control: Can disable monitoring
- Critical Alerts: AlarmManager ensures wake-up
- Auto-Recovery: Restarts after reboot
- Consistency: Reuses Workday-Wake patterns

### Negative
- Complexity: Multiple mechanisms
- Battery Impact: Foreground service notification
- User Experience: Requires exemption request
- Testing: Harder to test

## Alternatives Considered
1. WorkManager Only: Cannot guarantee timely execution
2. AlarmManager Only: Doesn't handle continuous monitoring
3. Foreground Service Only: Doesn't handle reboots

## References
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Workday-Wake AlarmGuardService](https://github.com/peterandree/Workday-Wake/blob/master/app/src/main/kotlin/com/workdaywake/alarm/AlarmGuardService.kt)