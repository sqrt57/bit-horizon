# Backlog

Open questions and decisions not yet made.

## Pending decisions / tasks

- **Scaffold the Kotlin Multiplatform + Compose Multiplatform project** — no Gradle project exists yet, only design docs. Needed before any real source code (including the big-number port below) can be added: Android/iOS/Desktop targets, `commonMain`/`androidMain`/`iosMain`/`desktopMain` source sets.
- **Port break_infinity.js to Kotlin** — for large-number representation (`Double` alone loses precision well before late-game idle-game scales). Decision: hand-roll a pure-Kotlin port of [break_infinity.js](https://patashu.github.io/break_infinity.js/index.html)'s `(mantissa, exponent)`-over-`Double` approach into `commonMain`, rather than depending on `BreakInfinity.java` (JVM-only, not usable from the iOS target) or `kotlin-multiplatform-bignum` (arbitrary-precision, KMP-safe, but heavier than idle games need). Blocked on the project scaffold above.

## Resolved decisions

- **Implementation platform** — switched from Android-native (Jetpack Compose) to **Compose Multiplatform**, targeting Android, iOS, and Desktop. Game logic is pure math with no platform-specific UI needs, so the switch costs little beyond Kotlin Multiplatform project setup (`commonMain`/`androidMain`/`iosMain`/`desktopMain` source sets) while keeping iOS/desktop open.
- **Persistence approach** — using shared Jetpack `DataStore` in `commonMain` for now: save state is a handful of scalars (Data Bits, three tier counts, Tachyon Particles, a timestamp), which fits DataStore's key-value model. Revisit with **SQLDelight** (multiplatform SQLite, native drivers for Android/iOS/Desktop) if the design grows something relational to query — e.g. persisted Log Hub history, multiple save slots, or an achievements table.
- **Large-number representation** — a hand-rolled Kotlin port of break_infinity.js, not a general BigDecimal library. See task above.
