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
}
