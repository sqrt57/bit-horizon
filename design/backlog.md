# Backlog

Open questions and decisions not yet made.

## Pending decisions

- **Large-number representation** — not specified in GDD. `Double` will lose precision well before late-game idle-game scales; needs `BigDecimal` or a custom notation type.

## Resolved decisions

- **Implementation platform** — switched from Android-native (Jetpack Compose) to **Compose Multiplatform**, targeting Android, iOS, and Desktop. Game logic is pure math with no platform-specific UI needs, so the switch costs little beyond Kotlin Multiplatform project setup (`commonMain`/`androidMain`/`iosMain`/`desktopMain` source sets) while keeping iOS/desktop open.
- **Persistence approach** — using shared Jetpack `DataStore` in `commonMain` for now: save state is a handful of scalars (Data Bits, three tier counts, Tachyon Particles, a timestamp), which fits DataStore's key-value model. Revisit with **SQLDelight** (multiplatform SQLite, native drivers for Android/iOS/Desktop) if the design grows something relational to query — e.g. persisted Log Hub history, multiple save slots, or an achievements table.
