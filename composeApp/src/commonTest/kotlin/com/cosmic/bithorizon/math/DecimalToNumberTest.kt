package com.cosmic.bithorizon.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalToNumberTest {

    @Test
    fun roundTripsWithinDoubleRange() {
        assertEquals(0.0, Decimal.fromDouble(0.0).toDouble())
        assertEquals(116.0, Decimal.fromDouble(116.0).toDouble())
        assertEquals(-116.0, Decimal.fromDouble(-116.0).toDouble())
        assertEquals(1.16, Decimal.fromDouble(1.16).toDouble())
        assertEquals(-1.16, Decimal.fromDouble(-1.16).toDouble())
        assertEquals(Double.MIN_VALUE, Decimal.fromDouble(Double.MIN_VALUE).toDouble())
        assertEquals(-Double.MIN_VALUE, Decimal.fromDouble(-Double.MIN_VALUE).toDouble())
    }

    @Test
    fun overflowAndUnderflow() {
        assertEquals(Double.POSITIVE_INFINITY, Decimal.fromString("6e900").toDouble())
        assertEquals(Double.NEGATIVE_INFINITY, Decimal.fromString("-6e900").toDouble())
        assertEquals(0.0, Decimal.fromString("6e-900").toDouble())
        assertEquals(0.0, Decimal.fromString("-6e-900").toDouble())
    }

    @Test
    fun infinityAndNaNPassThrough() {
        assertEquals(Double.POSITIVE_INFINITY, Decimal.fromDouble(Double.POSITIVE_INFINITY).toDouble())
        assertEquals(Double.NEGATIVE_INFINITY, Decimal.fromDouble(Double.NEGATIVE_INFINITY).toDouble())
        assertTrue(Decimal.fromDouble(Double.NaN).toDouble().isNaN())
    }
}
