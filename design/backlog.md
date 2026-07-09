# Backlog

Open questions, decisions, and tasks not yet done.

## Open questions

_None currently._

## Tasks

- **Port break_infinity.js to Kotlin** — hand-roll a pure-Kotlin port of [break_infinity.js](https://patashu.github.io/break_infinity.js/index.html)'s `(mantissa, exponent)`-over-`Double` approach into `commonMain`. See [large-number representation](#large-number-representation) below for why.

## Resolved decisions

- **Implementation platform** — switched from Android-native (Jetpack Compose) to **Compose Multiplatform**, targeting Android and Desktop. Game logic is pure math with no platform-specific UI needs, so the switch costs little beyond Kotlin Multiplatform project setup (`commonMain`/`androidMain`/`desktopMain` source sets).
- **iOS support** — deferred indefinitely: Kotlin/Native's iOS target requires Xcode on macOS to build, which isn't available in this environment (Windows). The KMP module structure keeps the door open to adding an `iosMain` source set later if Mac access becomes available.
- **Persistence approach** — using shared Jetpack `DataStore` in `commonMain` for now: save state is a handful of scalars (Data Bits, three tier counts, Tachyon Particles, a timestamp), which fits DataStore's key-value model. Revisit with **SQLDelight** (multiplatform SQLite, native drivers for Android/iOS/Desktop) if the design grows something relational to query — e.g. persisted Log Hub history, multiple save slots, or an achievements table.
- **Large-number representation** — `Double` alone loses precision well before late-game idle-game scales. Decision: a hand-rolled Kotlin port of break_infinity.js, not a general `BigDecimal` library — rejected `BreakInfinity.java` (JVM-only, not usable from the iOS target) and `kotlin-multiplatform-bignum` (arbitrary-precision, KMP-safe, but heavier than idle games need). See task above.
- **Project scaffold** — Kotlin Multiplatform + Compose Multiplatform Gradle project created: `composeApp` module with `commonMain`/`androidMain`/`desktopMain` source sets. Versions: Gradle 8.14, AGP 8.13.0, Kotlin 2.2.20, Compose Multiplatform 1.11.1, compileSdk/targetSdk 36, minSdk 26 (per GDD), JVM target 17. Both `assembleDebug` (Android) and `compileKotlinDesktop` (Desktop) verified building successfully.
