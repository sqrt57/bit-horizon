package com.cosmic.bithorizon.game

import com.cosmic.bithorizon.math.Decimal
import com.cosmic.bithorizon.math.affordGeometricSeries
import com.cosmic.bithorizon.math.sumGeometricSeries

/** Static cost curve for one infrastructure tier, from the GDD's Data Infrastructure Tiers table. */
data class TierConfig(
    val baseCost: Decimal,
    val costScale: Decimal,
)

/**
 * The three cascading infrastructure tiers. Each tier is purchased by spending the
 * resource one level down: Telemetry Sensors cost Data Bits, Sub-Probe Arrays cost
 * Telemetry Sensors, Quantum Relays cost Sub-Probe Arrays.
 */
enum class Tier(val config: TierConfig) {
    TELEMETRY_SENSOR(TierConfig(baseCost = Decimal.fromInt(10), costScale = Decimal.fromDouble(1.15))),
    SUB_PROBE_ARRAY(TierConfig(baseCost = Decimal.fromInt(10), costScale = Decimal.fromDouble(1.30))),
    QUANTUM_RELAY(TierConfig(baseCost = Decimal.fromInt(100), costScale = Decimal.fromDouble(1.50))),
}

/**
 * Current save state: Data Bits, the three infrastructure tier counts, and Tachyon
 * Particles accumulated from past Horizon Shifts.
 */
data class GameState(
    val dataBits: Decimal = Decimal.ZERO,
    val telemetrySensors: Decimal = Decimal.ZERO,
    val subProbeArrays: Decimal = Decimal.ZERO,
    val quantumRelays: Decimal = Decimal.ZERO,
    val tachyonParticles: Decimal = Decimal.ZERO,
) {
    fun count(tier: Tier): Decimal = when (tier) {
        Tier.TELEMETRY_SENSOR -> telemetrySensors
        Tier.SUB_PROBE_ARRAY -> subProbeArrays
        Tier.QUANTUM_RELAY -> quantumRelays
    }

    /** The balance spent to buy this tier: Data Bits for Tier 1, the tier below's count otherwise. */
    fun paymentBalance(tier: Tier): Decimal = when (tier) {
        Tier.TELEMETRY_SENSOR -> dataBits
        Tier.SUB_PROBE_ARRAY -> telemetrySensors
        Tier.QUANTUM_RELAY -> subProbeArrays
    }

    fun costForNext(tier: Tier): Decimal =
        sumGeometricSeries(Decimal.ONE, tier.config.baseCost, tier.config.costScale, count(tier))

    fun costFor(tier: Tier, amount: Decimal): Decimal =
        sumGeometricSeries(amount, tier.config.baseCost, tier.config.costScale, count(tier))

    fun maxAffordable(tier: Tier): Decimal =
        affordGeometricSeries(paymentBalance(tier), tier.config.baseCost, tier.config.costScale, count(tier))

    /** Buys as many of [tier] as the current payment balance allows. */
    fun buyMax(tier: Tier): GameState = buy(tier, maxAffordable(tier))

    /** Buys exactly [amount] of [tier]; returns this state unchanged if unaffordable. */
    fun buy(tier: Tier, amount: Decimal): GameState {
        if (amount.lte(Decimal.ZERO)) return this
        val cost = costFor(tier, amount)
        if (cost.gt(paymentBalance(tier))) return this
        return withPaymentBalance(tier, paymentBalance(tier).sub(cost)).withCount(tier, count(tier).add(amount))
    }

    private fun withPaymentBalance(tier: Tier, newBalance: Decimal): GameState = when (tier) {
        Tier.TELEMETRY_SENSOR -> copy(dataBits = newBalance)
        Tier.SUB_PROBE_ARRAY -> copy(telemetrySensors = newBalance)
        Tier.QUANTUM_RELAY -> copy(subProbeArrays = newBalance)
    }

    private fun withCount(tier: Tier, newCount: Decimal): GameState = when (tier) {
        Tier.TELEMETRY_SENSOR -> copy(telemetrySensors = newCount)
        Tier.SUB_PROBE_ARRAY -> copy(subProbeArrays = newCount)
        Tier.QUANTUM_RELAY -> copy(quantumRelays = newCount)
    }
}
