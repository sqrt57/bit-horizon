# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BitHorizon is a cross-platform incremental (idle) game: cascading mathematical production loops with a dark-terminal UI aesthetic. Full design spec lives in `design/gdd.md` (core loop, math engine, prestige mechanics, UI layout); open questions and pending tasks are tracked in `design/backlog.md`; resolved-decision rationale (e.g. why break_infinity.js over BigDecimal, why DataStore over SQLDelight) lives in `design/changelog.md` — check all three before making product/architecture decisions, since that rationale shouldn't be re-litigated.

- Package: `com.cosmic.bithorizon`
- Targets: Android (minSdk 26) and Desktop (Windows/macOS/Linux). iOS is deferred indefinitely — no macOS/Xcode build environment available; the KMP module structure keeps an `iosMain` source set addable later.
- UI: Compose Multiplatform (Material 3)

Note: `README.md` at the repo root predates the switch to Compose Multiplatform and still describes the project as Android-native only — `design/gdd.md` and `design/backlog.md` are the current source of truth.

## Commands

Build/run via the Gradle wrapper (`gradlew`/`gradlew.bat`) from the repo root.

```
./gradlew assembleDebug            # Android debug APK
./gradlew compileKotlinDesktop     # compile the desktop target
./gradlew :composeApp:run          # run the desktop app
```

There is no lint configuration yet. Tests live in `composeApp/src/commonTest`; run them with:

```
./gradlew :composeApp:desktopTest      # runs the commonTest suite on the JVM/desktop target
```

## Architecture

Single Gradle project (`BitHorizon`) with one Kotlin Multiplatform module, `composeApp`, split into source sets:

- `commonMain` — shared game logic and UI (Compose). This is where all math-engine and game-state code belongs, since none of the design's mechanics are platform-specific.
- `androidMain` — `MainActivity.kt`, Android manifest/resources.
- `desktopMain` — `main.kt`, the JVM/desktop entry point.

The Compose root composable is `App()` in `commonMain/.../App.kt`; both platform entry points call into it. The UI is designed around four fixed vertical zones (telemetry metrics, log hub, infrastructure tiers, executive core/prestige button) as specified in `design/gdd.md` — currently only scaffolded with TODO placeholders.

Versions are centralized in `gradle/libs.versions.toml` (version catalog): AGP 8.13.0, Kotlin 2.2.20, Compose Multiplatform 1.11.1, Gradle 8.14, compileSdk/targetSdk 36, JVM target 17.

### Math engine

The GDD's math depends on numbers that exceed `Double` precision at late-game scale. `com.cosmic.bithorizon.math.Decimal` (`commonMain/.../math/`) is a hand-rolled Kotlin port of break_infinity.js's `(mantissa, exponent)`-over-`Double` representation — not a general-purpose BigDecimal library. Use this type, not raw `Double`, for game math (cost scaling, production ticks, prestige multipliers). It's immutable (every operation returns a new instance); construct values via `Decimal.fromDouble`/`fromInt`/`fromString`/`fromMantissaExponent`, not a public constructor. `DecimalMath.kt` in the same package has the geometric/arithmetic series purchase helpers (`affordGeometricSeries`, `sumGeometricSeries`, etc.) that back "how many can I afford" / "cost of buying N more" calculations. See `design/changelog.md`'s "break_infinity.js port" entry for the porting decisions (why immutable, why exponent is `Double`, what was dropped from upstream) if extending this type.

### Game state

`com.cosmic.bithorizon.game.GameState` (`commonMain/.../game/GameState.kt`) is the immutable save-state model: Data Bits, the three tier counts (Telemetry Sensors, Sub-Probe Arrays, Quantum Relays), and Tachyon Particles, all as `Decimal`. `Tier` is an enum carrying each tier's `TierConfig` (base cost, cost scale, base output/sec) straight from the GDD's tier table. Buy-cost math (`costForNext`, `costFor`, `maxAffordable`, `buy`, `buyMax`) is built on `DecimalMath`'s geometric-series helpers rather than reimplemented. `tick(deltaSeconds)` applies the GDD's delta-ticker formula once (default `Δt = TICK_DELTA_SECONDS = 0.1s`): each tier's count produces the resource one level down (Quantum Relays → Sub-Probe Arrays → Telemetry Sensors → Data Bits), scaled by `(1 + Tachyon Particles)` for the permanent prestige efficiency bonus. All three tiers' output is computed from the pre-tick snapshot before anything is written, so production doesn't cascade within one tick. There's no real-time driver yet (no coroutine loop calling `tick()` on a timer) — that's expected to land when the UI is wired up, via a `LaunchedEffect` in the Compose layer.

`canHorizonShift()`/`horizonShift()` implement the GDD's Horizon Shift prestige mechanic: once Data Bits reach `HORIZON_SHIFT_THRESHOLD` (10,000), `horizonShift()` wipes Data Bits and all three tier counts and grants +1 Tachyon Particle (which `tick()` already turns into a permanent efficiency bonus). `horizonShift()` no-ops below the threshold, mirroring `buy()`'s pattern of returning the unchanged state rather than throwing when a precondition isn't met.
