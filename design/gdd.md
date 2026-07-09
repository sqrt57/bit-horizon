# Game Design Document: Project BitHorizon

**GitHub Repository Description:** 🌌 A cross-platform incremental game utilizing cascading mathematical production loops and dark-terminal aesthetics. Infinite scaling, finite UI boundaries.
**Package Structure:** `com.cosmic.bithorizon`
**Target Platforms:** Android (Minimum SDK 26), Desktop (Windows/macOS/Linux). iOS deferred indefinitely (no macOS/Xcode build environment available).
**UI Framework:** Compose Multiplatform (Material 3)

---

## 1. Game Overview & Core Loop
You are an autonomous interstellar data processor navigating the cosmic void. Your task is to extract information from cosmic background radiation, scale your telemetry infrastructure, and continuously break through variables limits. 

The gameplay loop balances manual optimization, automated scaling, and a soft-prestige layer that resets current physical systems in exchange for fundamental subatomic processing power.

---

## 2. Mathematical Engine

The system uses a cascading nested multiplier setup. Tier $n$ feeds directly into the quantitative growth of Tier $n-1$.

### Core Formulas
*   **Cost Scaling:**  
    $$\text{Cost} = \text{BaseCost} \times (\text{Multiplier})^{\text{AmountOwned}}$$
*   **Delta Ticker Loop:** Calculations process 10 times a second ($\Delta t = 0.1$s):  
    $$\Delta \text{Tier}_{n-1} = \text{Tier}_n \times \text{Multiplier}_n \times \Delta t$$
*   **Prestige Modifier:**  
    $$\text{Total Bits/Sec} = \text{Base Bits/Sec} \times (1 + \text{Tachyon Particles})$$

### Data Infrastructure Tiers

| Tier | System Component | Resource Cost | Base Cost | Cost Scale | Base Output (Per Second) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Tier 1** | Telemetry Sensor | Data Bits | 10 Bits | 1.15 | +1.0 Data Bit |
| **Tier 2** | Sub-Probe Array | Telemetry Sensors | 10 Sensors | 1.30 | +1.0 Telemetry Sensor |
| **Tier 3** | Quantum Relay | Sub-Probe Arrays | 100 Arrays | 1.50 | +1.0 Sub-Probe Array |

---

## 3. Prestige Mechanics: Horizon Shift

When information gathering approaches local saturation points, you must flush the system to expand your processing horizon.

*   **Unlock Threshold:** $10,000$ ($1.00\text{e}4$) Data Bits.
*   **Trigger Event:** The execution button morphs to **[HORIZON SHIFT]**.
*   **The Wipe:** Resets Data Bits, Telemetry Sensors, Sub-Probe Arrays, and Quantum Relays.
*   **The Permanent Gain:** Yields **+1 Tachyon Particle**. Each active particle permanently applies a global $+100\%$ efficiency bonus to all production output channels in subsequent runs.

---

## 4. UI Layout Definition

Single-screen structure designed for responsive, thumb-friendly dark terminal view (`0xFF0A0A0C`).

```text
+---------------------------------------------------+
|  ZONE 1: TELEMETRY METRICS                        |
|  Displays raw Data Bits (Scientific format) & Bps  |
+---------------------------------------------------+
|  ZONE 2: THE LOG HUB                              |
|  Monospaced text displaying real-time system events|
+---------------------------------------------------+
|  ZONE 3: INFRASTRUCTURE TIERS                     |
|  Vertical stack containing counts and buy actions  |
+---------------------------------------------------+
|  ZONE 4: EXECUTIVE CORE                           |
|  Large touch-target for Manual Scan/Horizon Shift |
+---------------------------------------------------+
```
