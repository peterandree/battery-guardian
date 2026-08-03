# Battery Guardian

Android application that monitors, predicts, and alerts users about the battery levels of connected Bluetooth devices.

---

## Commands

```bash
# Unit tests (JVM only, fast)
./gradlew test

# Single test class
./gradlew test --tests "com.batteryguardian.domain.usecase.*"

# Lint
./gradlew lint

# Pre-push check — run before every PR
./gradlew lint test assembleDebug

# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

Run `./gradlew lint test` before marking any task done.

---

## Tech Stack

| Layer | Library / Version |
| --- | --- |
| Language | Kotlin, JVM 17 |
| SDK | minSdk 31, targetSdk 35 |
| UI | Jetpack Compose + Material3 |
| Navigation | Compose Navigation |
| DI | Hilt (KSP) |
| DB | Room (KSP, schema exported) |
| Preferences | DataStore Preferences |
| Async | Coroutines |
| Testing | JUnit 5, Truth, Turbine, MockK, Robolectric, Room Testing |
| Bluetooth | Android Bluetooth API |

---

## Project Structure

```
docs/
  adr/                          — Architectural Decision Records
  architecture.md               — Layer diagram and conventions
  requirements.md               — Functional and technical requirements

app/src/main/kotlin/com/batteryguardian/
  MainActivity.kt                — Entry point, NavHost
  BatteryGuardianApplication.kt  — @HiltAndroidApp, notification channel init
  
  monitoring/                    — Bluetooth monitoring components
    BatteryMonitorService.kt     — Foreground service for monitoring
    GattBatteryReader.kt          — GATT-based battery reading
    ClassicBatteryReader.kt       — Classic Bluetooth battery reading
    BatteryPredictionEngine.kt   — Linear regression for battery predictions
    PollingOrchestrator.kt        — Manages periodic battery checks
    Scanner.kt                    — Device discovery and scanning
    
  data/
    local/                        — Room DAOs, entities, repositories
      BatteryDatabase.kt           — Room database
      DeviceDao.kt                 — Device data access
      BatteryLevelDao.kt           — Battery history access
      BatteryHealthRepository.kt   — Battery data repository
    preferences/                  — User preferences
      UserPreferencesRepository.kt — All DataStore keys
    
  di/
    AppModule.kt                  — All Hilt bindings
    
  domain/
    model/                        — Pure Kotlin data classes
      Device.kt                    — Device model
      BatteryLevel.kt              — Battery level model
      BatteryPrediction.kt        — Prediction model
      AlertState.kt                — Alert state model
    repository/                   — Repository interfaces
      DeviceRepository.kt          — Device repository interface
      BatteryRepository.kt         — Battery repository interface
      AlertRepository.kt           — Alert repository interface
    usecase/                      — Business logic
      MonitorBatteryUseCase.kt    — Battery monitoring logic
      PredictBatteryUseCase.kt    — Battery prediction logic
      AlertUseCase.kt              — Alert triggering logic
      ManageDevicesUseCase.kt      — Device management logic
      
  ui/
    MainScreen.kt                — Main device list screen
    DeviceDetailScreen.kt        — Device details and history
    SettingsScreen.kt             — User settings
    theme/                        — Theme and styling
    components/                   — Reusable UI components
    
app/src/test/                     — JVM unit tests
app/src/androidTest/              — Instrumented tests
app/schemas/                      — Room schema JSON exports (do NOT edit manually)
```

---

## Architecture Rules

- **Layer order**: UI → ViewModel → UseCase → Repository → DAO. Never skip layers.
- ViewModel must never access a DAO directly — go through Repository or UseCase.
- `StateFlow` everywhere — never `LiveData`.
- `@Inject constructor` always — field injection only in `BroadcastReceiver` and `Activity`.
- All Hilt bindings in `AppModule.kt` — do not create new Hilt module files.
- All DataStore keys defined in `UserPreferencesRepository` — never inline.
- BroadcastReceivers: `goAsync()` + `SupervisorJob` + coroutine scope — never block `onReceive`.

---

## Code Style

- 4-space indent, 120-char line limit (`.editorconfig` enforces)
- No wildcard imports
- Coroutines in ViewModels: `viewModelScope`
- Coroutines in BroadcastReceivers: `goAsync()` + `SupervisorJob`

**Exception Handling**
- Exceptions should never be swallowed. Always log or handle exceptions explicitly; do not use empty catch blocks or ignore errors.

**✅ ViewModel calling repository:**
```kotlin
fun checkBatteryLevels() {
    viewModelScope.launch {
        batteryRepository.getAllDevices()
            .collect { devices ->
                // Update UI state
            }
    }
}
```

**❌ ViewModel calling DAO directly:**
```kotlin
fun checkBatteryLevels() {
    viewModelScope.launch {
        deviceDao.getAll() // never do this
    }
}
```

**✅ BroadcastReceiver async pattern:**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    val result = goAsync()
    CoroutineScope(SupervisorJob()).launch {
        try { /* work */ } finally { result.finish() }
    }
}
```

---

## Commit Format

```
<type>(<scope>): <summary in present tense, max 72 chars>
```

Types: `feat` `fix` `chore` `refactor` `test` `docs`  
Scopes: `monitoring` `ui` `data` `domain` `di` `bluetooth` `ci` `alerts`

Examples:
- `feat(monitoring): add GATT battery level reading`
- `fix(bluetooth): handle device disconnection gracefully`
- `test(domain): add BatteryPredictionEngine edge case tests`
- `docs: update architecture.md with monitoring flow`

---

## Issue & PR Conventions

- Issues follow Epic → Story → Task hierarchy with sub-issue links
- `// TODO` comments must include a linked issue number: `// TODO #123`
- PRs reference issues with `Closes #N` in the description
- An ADR in `docs/adr/` is required when: adding a library, changing Room schema, introducing a new layer, or changing monitoring strategy

---

## Agent Boundaries

**Always:**
- Write to `app/src/` only
- Run `./gradlew lint test` before marking a task done
- Follow layer rules — never skip ViewModel → Repository
- Read the existing file before editing; never duplicate logic that already exists
- Use existing DAOs/repositories before creating new ones

**Ask first:**
- Adding a new third-party dependency
- Changing the Room schema
- Creating a new Hilt module
- Modifying `AndroidManifest.xml` permissions

**Never:**
- Commit API keys or secrets
- Edit `app/schemas/` JSON manually
- Use `LiveData`
- Create DataStore keys outside `UserPreferencesRepository`
- Add field injection outside Receivers and Activities