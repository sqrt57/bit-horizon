package com.cosmic.bithorizon.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalMantissaWithDecimalPlacesTest {

    @Test
    fun zero() {
        assertEquals(0.0, Decimal.fromDouble(0.0).mantissaWithDecimalPlaces(2))
    }

    @Test
    fun shortMantissa() {
        assertEquals(1.2, Decimal.fromDouble(12.0).mantissaWithDecimalPlaces(2))
    }

    @Test
    fun longValueRounds() {
        assertEquals(1.23, Decimal.fromDouble(12345.0).mantissaWithDecimalPlaces(2))
    }

    @Test
    fun infinityAndNaNPassThrough() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            Decimal.fromDouble(Double.POSITIVE_INFINITY).mantissaWithDecimalPlaces(2),
        )
        assertEquals(
            Double.NEGATIVE_INFINITY,
            Decimal.fromDouble(Double.NEGATIVE_INFINITY).mantissaWithDecimalPlaces(2),
        )
        assertTrue(Decimal.fromDouble(Double.NaN).mantissaWithDecimalPlaces(2).isNaN())
    }
}
