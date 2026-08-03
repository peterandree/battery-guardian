# Battery Guardian

[![CI](https://github.com/peterandree/battery-guardian/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/peterandree/battery-guardian/actions/workflows/ci.yml)

**Monitor, predict, and alert battery levels for your Bluetooth devices.**

---

## Features

- **Real-time Monitoring**: Track battery levels of all connected Bluetooth devices
- **Predictive Alerts**: Get notifications when your device will hit low battery based on current drain rate using linear regression
- **Battery Health Tracking**: Monitor long-term battery degradation
- **Custom Thresholds**: Set your own alert levels for each device (default: 20%, 10%, 5%)
- **Background Operation**: Works even when the app is closed
- **Hysteresis**: Prevents notification spam with intelligent threshold handling (2% band)
- **Device Management**: Ignore specific devices, customize names, and view history

---

## Supported Devices

Battery Guardian works with any Bluetooth device that supports:
- **BLE Battery Service** (UUID: 0000180f-0000-1000-8000-00805f9b34fb)
- **Classic Bluetooth battery reporting** (via Android broadcasts)

Initially tested with:
- Sennheiser Accentum Wireless
- ThinkPlus GM2

---

## Screenshots

*(To be added as development progresses)*

---

## Installation

### Prerequisites

- Android 12 (API 31) or later
- Bluetooth enabled on your device
- Bluetooth devices paired with your phone

### From Source

1. Clone the repository:
   ```bash
   git clone https://github.com/peterandree/battery-guardian.git
   ```
2. Open in Android Studio
3. Build and run on your device

---

## Build Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Meerkat (2024.3.1) or later |
| JDK | 17 |
| Android SDK | 35 (compile), 31 minimum |
| Kotlin | 2.0.21 |
| Gradle | 8.7 |

---

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

---

## Architecture

Battery Guardian follows a clean layered architecture:
- **UI Layer**: Jetpack Compose
- **Domain Layer**: Pure Kotlin business logic
- **Data Layer**: Room database, Bluetooth API integration
- **Platform Layer**: Android-specific services and receivers

See [docs/architecture.md](docs/architecture.md) for the detailed technical architecture.

---

## Documentation

- [Requirements](docs/requirements.md) — Detailed functional and technical requirements
- [Architecture](docs/architecture.md) — Technical architecture overview
- [ADRs](docs/adr/) — Architectural Decision Records

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.