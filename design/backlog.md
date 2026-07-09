# Backlog

Open questions, decisions, and tasks not yet done.

## Open questions

_None currently._

## Tasks

_None currently._

## Resolved decisions

- **Implementation platform** — switched from Android-native (Jetpack Compose) to **Compose Multiplatform**, targeting Android and Desktop. Game logic is pure math with no platform-specific UI needs, so the switch costs little beyond Kotlin Multiplatform project setup (`commonMain`/`androidMain`/`desktopMain` source sets).
- **iOS support** — deferred indefinitely: Kotlin/Native's iOS target requires Xcode on macOS to build, which isn't available in this environment (Windows). The KMP module structure keeps the door open to adding an `iosMain` source set later if Mac access becomes available.
- **Persistence approach** — using shared Jetpack `DataStore` in `commonMain` for now: save state is a handful of scalars (Data Bits, three tier counts, Tachyon Particles, a timestamp), which fits DataStore's key-value model. Revisit with **SQLDelight** (multiplatform SQLite, native drivers for Android/iOS/Desktop) if the design grows something relational to query — e.g. persisted Log Hub history, multiple save slots, or an achievements table.
- **Large-number representation** — `Double` alone loses precision well before late-game idle-game scales. Decision: a hand-rolled Kotlin port of break_infinity.js, not a general `BigDecimal` library — rejected `BreakInfinity.java` (JVM-only, not usable from the iOS target) and `kotlin-multiplatform-bignum` (arbitrary-precision, KMP-safe, but heavier than idle games need).
- **break_infinity.js port** — `Decimal` (`composeApp/src/commonMain/kotlin/com/cosmic/bithorizon/math/`) ported with full API parity (arithmetic, comparisons incl. tolerance variants, rounding, log/pow/sqrt/cbrt, string formatting, factorial/hyperbolic/joke functions) plus the `math.ts` series-purchase helpers (`affordGeometricSeries` etc.). Immutable design (not upstream's mutate-during-construction pattern); exponent kept as `Double` to match JS `Number` semantics exactly (`EXP_LIMIT = 9e15` exceeds `Int` range). String formatting (`toString`/`toFixed`/`toExponential`/`toPrecision`) is hand-rolled directly from the `(mantissa, exponent)` pair rather than delegating to `Double.toString()`, since JVM and JS switch to scientific notation at different magnitude thresholds and `String.format`/`java.text.*` aren't available from `commonMain` anyway — keeps the door open for a future `iosMain` target. Dropped upstream's `decimal-pool.ts` caching (JS-GC-pressure optimization not worth it on JVM) and the companion-mirrored static methods (e.g. `Decimal.add(a, b)`) since Kotlin call sites just use `a.add(b)`. Test suite ported to `commonTest` (construction round-tripping, comparison sign correctness, string-formatting at exponent boundaries, `toDouble()` overflow/underflow, mantissa rounding) using `kotlin.test`.
- **Project scaffold** — Kotlin Multiplatform + Compose Multiplatform Gradle project created: `composeApp` module with `commonMain`/`androidMain`/`desktopMain` source sets. Versions: Gradle 8.14, AGP 8.13.0, Kotlin 2.2.20, Compose Multiplatform 1.11.1, compileSdk/targetSdk 36, minSdk 26 (per GDD), JVM target 17. Both `assembleDebug` (Android) and `compileKotlinDesktop` (Desktop) verified building successfully.
