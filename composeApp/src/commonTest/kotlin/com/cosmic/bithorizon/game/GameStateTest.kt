package com.cosmic.bithorizon.game

import com.cosmic.bithorizon.math.Decimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameStateTest {

    @Test
    fun costForNextMatchesBaseCostWhenNoneOwned() {
        val state = GameState()
        assertEquals(0, state.costForNext(Tier.TELEMETRY_SENSOR).cmp(Decimal.fromInt(10)))
        assertEquals(0, state.costForNext(Tier.SUB_PROBE_ARRAY).cmp(Decimal.fromInt(10)))
        assertEquals(0, state.costForNext(Tier.QUANTUM_RELAY).cmp(Decimal.fromInt(100)))
    }

    @Test
    fun costForNextRisesWithOwnedCount() {
        val state = GameState(subProbeArrays = Decimal.fromInt(1))
        // 10 * 1.30^1 = 13
        assertEquals(0, state.costForNext(Tier.SUB_PROBE_ARRAY).cmp(Decimal.fromDouble(13.0)))
    }

    @Test
    fun buyDeductsPaymentBalanceAndIncrementsCount() {
        val state = GameState(dataBits = Decimal.fromInt(10))
        val bought = state.buy(Tier.TELEMETRY_SENSOR, Decimal.ONE)
        assertEquals(0, bought.telemetrySensors.cmp(Decimal.ONE))
        assertEquals(0, bought.dataBits.cmp(Decimal.ZERO))
    }

    @Test
    fun buyLeavesStateUnchangedWhenUnaffordable() {
        val state = GameState(dataBits = Decimal.fromInt(5))
        val bought = state.buy(Tier.TELEMETRY_SENSOR, Decimal.ONE)
        assertEquals(0, bought.dataBits.cmp(Decimal.fromInt(5)))
        assertEquals(0, bought.telemetrySensors.cmp(Decimal.ZERO))
    }

    @Test
    fun buyMaxSpendsAsMuchAsAffordableAndNoMore() {
        val state = GameState(dataBits = Decimal.fromInt(100))
        val bought = state.buyMax(Tier.TELEMETRY_SENSOR)
        assertTrue(bought.telemetrySensors.gt(Decimal.ZERO))
        assertTrue(bought.dataBits.gte(Decimal.ZERO))
        // Buying one more unit should now be unaffordable.
        val next = bought.costForNext(Tier.TELEMETRY_SENSOR)
        assertTrue(next.gt(bought.dataBits))
    }

    @Test
    fun buyingSubProbeArraySpendsTelemetrySensorsNotDataBits() {
        val state = GameState(dataBits = Decimal.fromInt(1000), telemetrySensors = Decimal.fromInt(10))
        val bought = state.buy(Tier.SUB_PROBE_ARRAY, Decimal.ONE)
        assertEquals(0, bought.dataBits.cmp(Decimal.fromInt(1000)))
        assertEquals(0, bought.telemetrySensors.cmp(Decimal.ZERO))
        assertEquals(0, bought.subProbeArrays.cmp(Decimal.ONE))
    }

    @Test
    fun buyingZeroOrNegativeAmountIsNoOp() {
        val state = GameState(dataBits = Decimal.fromInt(10))
        assertEquals(0, state.buy(Tier.TELEMETRY_SENSOR, Decimal.ZERO).dataBits.cmp(Decimal.fromInt(10)))
        assertEquals(0, state.buy(Tier.TELEMETRY_SENSOR, Decimal.fromInt(-1)).dataBits.cmp(Decimal.fromInt(10)))
    }

    @Test
    fun tickProducesDataBitsFromTelemetrySensors() {
        val state = GameState(telemetrySensors = Decimal.fromInt(10))
        val ticked = state.tick(deltaSeconds = 0.1)
        // 10 sensors * 1.0/sec * (1 + 0 particles) * 0.1s = 1.0
        assertEquals(0, ticked.dataBits.cmp(Decimal.ONE))
        assertEquals(0, ticked.telemetrySensors.cmp(Decimal.fromInt(10)))
    }

    @Test
    fun tickCascadesAllThreeTiersFromOneSnapshot() {
        val state = GameState(
            telemetrySensors = Decimal.fromInt(10),
            subProbeArrays = Decimal.fromInt(20),
            quantumRelays = Decimal.fromInt(30),
        )
        val ticked = state.tick(deltaSeconds = 0.1)
        // Each tier's output uses the pre-tick count, not a value already bumped this tick.
        assertEquals(0, ticked.dataBits.cmp(Decimal.fromDouble(1.0))) // 10 * 0.1
        assertEquals(0, ticked.telemetrySensors.cmp(Decimal.fromDouble(12.0))) // 10 + 20 * 0.1
        assertEquals(0, ticked.subProbeArrays.cmp(Decimal.fromDouble(23.0))) // 20 + 30 * 0.1
        assertEquals(0, ticked.quantumRelays.cmp(Decimal.fromInt(30))) // nothing feeds Tier 3
    }

    @Test
    fun tickAppliesTachyonEfficiencyBonusToAllChannels() {
        val state = GameState(telemetrySensors = Decimal.fromInt(10), tachyonParticles = Decimal.fromInt(2))
        val ticked = state.tick(deltaSeconds = 0.1)
        // 10 sensors * 1.0/sec * (1 + 2 particles) * 0.1s = 3.0
        assertEquals(0, ticked.dataBits.cmp(Decimal.fromDouble(3.0)))
    }

    @Test
    fun tickWithNothingOwnedProducesNothing() {
        val ticked = GameState().tick()
        assertEquals(0, ticked.dataBits.cmp(Decimal.ZERO))
    }

    @Test
    fun canHorizonShiftIsFalseBelowThreshold() {
        val state = GameState(dataBits = Decimal.fromInt(9_999))
        assertEquals(false, state.canHorizonShift())
    }

    @Test
    fun canHorizonShiftIsTrueAtThreshold() {
        val state = GameState(dataBits = Decimal.fromInt(10_000))
        assertEquals(true, state.canHorizonShift())
    }

    @Test
    fun horizonShiftIsNoOpBelowThreshold() {
        val state = GameState(
            dataBits = Decimal.fromInt(9_999),
            telemetrySensors = Decimal.fromInt(5),
            tachyonParticles = Decimal.fromInt(1),
        )
        val shifted = state.horizonShift()
        assertEquals(0, shifted.dataBits.cmp(Decimal.fromInt(9_999)))
        assertEquals(0, shifted.telemetrySensors.cmp(Decimal.fromInt(5)))
        assertEquals(0, shifted.tachyonParticles.cmp(Decimal.ONE))
    }

    @Test
    fun horizonShiftWipesResourcesAndGrantsOneTachyonParticle() {
        val state = GameState(
            dataBits = Decimal.fromInt(15_000),
            telemetrySensors = Decimal.fromInt(50),
            subProbeArrays = Decimal.fromInt(20),
            quantumRelays = Decimal.fromInt(3),
        )
        val shifted = state.horizonShift()
        assertEquals(0, shifted.dataBits.cmp(Decimal.ZERO))
        assertEquals(0, shifted.telemetrySensors.cmp(Decimal.ZERO))
        assertEquals(0, shifted.subProbeArrays.cmp(Decimal.ZERO))
        assertEquals(0, shifted.quantumRelays.cmp(Decimal.ZERO))
        assertEquals(0, shifted.tachyonParticles.cmp(Decimal.ONE))
    }

    @Test
    fun horizonShiftAccumulatesTachyonParticlesAcrossRuns() {
        val state = GameState(dataBits = Decimal.fromInt(10_000), tachyonParticles = Decimal.fromInt(4))
        val shifted = state.horizonShift()
        assertEquals(0, shifted.tachyonParticles.cmp(Decimal.fromInt(5)))
    }

    @Test
    fun dataBitsPerSecondReflectsTelemetrySensorsAndTachyonBonus() {
        val state = GameState(telemetrySensors = Decimal.fromInt(10), tachyonParticles = Decimal.fromInt(1))
        // 10 sensors * 1.0/sec * (1 + 1 particle) = 20
        assertEquals(0, state.dataBitsPerSecond().cmp(Decimal.fromInt(20)))
    }

    @Test
    fun manualScanAddsFlatYieldToDataBits() {
        val state = GameState(dataBits = Decimal.fromInt(5))
        val scanned = state.manualScan()
        assertEquals(0, scanned.dataBits.cmp(Decimal.fromInt(5).add(MANUAL_SCAN_YIELD)))
    }
}
