package com.cosmic.bithorizon.math

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalStringFormatTest {

    private val hugePositive900 = "6" + "0".repeat(900) + ".00"
    private val hugeNegative900 = "-6" + "0".repeat(900) + ".00"

    @Test
    fun toStringCases() {
        assertEquals("0", Decimal.fromDouble(0.0).toString())
        assertEquals("116", Decimal.fromDouble(116.0).toString())
        assertEquals("-116", Decimal.fromDouble(-116.0).toString())
        assertEquals("1.16", Decimal.fromDouble(1.16).toString())
        assertEquals("-1.16", Decimal.fromDouble(-1.16).toString())
        assertEquals("6e+900", Decimal.fromString("6e900").toString())
        assertEquals("-6e+900", Decimal.fromString("-6e900").toString())
        assertEquals("6e-900", Decimal.fromString("6e-900").toString())
        assertEquals("-6e-900", Decimal.fromString("-6e-900").toString())
        assertEquals("Infinity", Decimal.fromDouble(Double.POSITIVE_INFINITY).toString())
        assertEquals("-Infinity", Decimal.fromDouble(Double.NEGATIVE_INFINITY).toString())
        assertEquals("NaN", Decimal.fromDouble(Double.NaN).toString())
    }

    @Test
    fun toStringAtExponentBoundaries() {
        // Regression coverage for the hand-rolled plain-decimal formatter that replaces
        // upstream's reliance on JS's native Number.toString() - see design/backlog.md.
        assertEquals("1e-7", Decimal.fromMantissaExponent(1.0, -7.0).toString())
        assertEquals("0.000001", Decimal.fromMantissaExponent(1.0, -6.0).toString())
        assertEquals("10000000000", Decimal.fromMantissaExponent(1.0, 10.0).toString())
        assertEquals("1000000000000000", Decimal.fromMantissaExponent(1.0, 15.0).toString())
        assertEquals("100000000000000000000", Decimal.fromMantissaExponent(1.0, 20.0).toString())
        assertEquals("1e+21", Decimal.fromMantissaExponent(1.0, 21.0).toString())
    }

    @Test
    fun toExponentialCases() {
        assertEquals("0.00e+0", Decimal.fromDouble(0.0).toExponential(2))
        assertEquals("1.16e+2", Decimal.fromDouble(116.0).toExponential(2))
        assertEquals("1.16e+0", Decimal.fromDouble(1.16).toExponential(2))
        assertEquals("6.00e+900", Decimal.fromString("6e900").toExponential(2))
        assertEquals("-6.00e+900", Decimal.fromString("-6e900").toExponential(2))
        assertEquals("6.00e-900", Decimal.fromString("6e-900").toExponential(2))
        assertEquals("-6.00e-900", Decimal.fromString("-6e-900").toExponential(2))
        assertEquals("Infinity", Decimal.fromDouble(Double.POSITIVE_INFINITY).toExponential(2))
        assertEquals("-Infinity", Decimal.fromDouble(Double.NEGATIVE_INFINITY).toExponential(2))
        assertEquals("NaN", Decimal.fromDouble(Double.NaN).toExponential(2))
    }

    @Test
    fun toFixedCases() {
        assertEquals("0.00", Decimal.fromDouble(0.0).toFixed(2))
        assertEquals("116.00", Decimal.fromDouble(116.0).toFixed(2))
        assertEquals("1.16", Decimal.fromDouble(1.16).toFixed(2))
        assertEquals(hugePositive900, Decimal.fromString("6e900").toFixed(2))
        assertEquals(hugeNegative900, Decimal.fromString("-6e900").toFixed(2))
        assertEquals("0.00", Decimal.fromString("6e-900").toFixed(2))
        assertEquals("0.00", Decimal.fromString("-6e-900").toFixed(2))
        assertEquals("Infinity", Decimal.fromDouble(Double.POSITIVE_INFINITY).toFixed(2))
        assertEquals("-Infinity", Decimal.fromDouble(Double.NEGATIVE_INFINITY).toFixed(2))
        assertEquals("NaN", Decimal.fromDouble(Double.NaN).toFixed(2))
    }

    @Test
    fun toFixedAtMaxSignificantDigitsBoundary() {
        // e == MAX_SIGNIFICANT_DIGITS (17): the plain-decimal path must still print
        // as a full (unrounded) integer, not switch representation.
        assertEquals(
            "100000000000000000.00",
            Decimal.fromMantissaExponent(1.0, 17.0).toFixed(2),
        )
    }

    @Test
    fun toPrecisionCases() {
        assertEquals("0.0", Decimal.fromDouble(0.0).toPrecision(2))
        assertEquals("1.2e+2", Decimal.fromDouble(116.0).toPrecision(2))
        assertEquals("1.2", Decimal.fromDouble(1.16).toPrecision(2))
        assertEquals("6.0e+900", Decimal.fromString("6e900").toPrecision(2))
        assertEquals("-6.0e+900", Decimal.fromString("-6e900").toPrecision(2))
        assertEquals("6.0e-900", Decimal.fromString("6e-900").toPrecision(2))
        assertEquals("-6.0e-900", Decimal.fromString("-6e-900").toPrecision(2))
        assertEquals("Infinity", Decimal.fromDouble(Double.POSITIVE_INFINITY).toPrecision(2))
        assertEquals("-Infinity", Decimal.fromDouble(Double.NEGATIVE_INFINITY).toPrecision(2))
        assertEquals("NaN", Decimal.fromDouble(Double.NaN).toPrecision(2))
    }
}
