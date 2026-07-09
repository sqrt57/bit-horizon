package com.cosmic.bithorizon.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalConstructionTest {

    private fun assertME(decimal: Decimal, mantissa: Double, exponent: Double) {
        assertEquals(mantissa, decimal.mantissa, "mantissa")
        assertEquals(exponent, decimal.exponent, "exponent")
    }

    private fun assertNaNDecimal(decimal: Decimal) {
        assertTrue(decimal.mantissa.isNaN(), "expected NaN mantissa")
        assertEquals(0.0, decimal.exponent)
    }

    @Test
    fun finiteNumbersAndStringCounterparts() {
        val cases = listOf(
            Triple(0.0, "0", 0.0 to 0.0),
            Triple(Double.MIN_VALUE, "5e-324", 5.0 to -324.0),
            Triple(1e-323, "1e-323", 1.0 to -323.0),
            Triple(0.1, "0.1", 1.0 to -1.0),
            Triple(1.0, "1", 1.0 to 0.0),
            Triple(1.1, "1.1", 1.1 to 0.0),
            Triple(10.0, "10", 1.0 to 1.0),
            Triple(1e10, "1e10", 1.0 to 10.0),
            Triple(1e308, "1e308", 1.0 to 308.0),
            Triple(Double.MAX_VALUE, "1.7976931348623157e308", 1.7976931348623157 to 308.0),
            Triple(-0.0, "-0", 0.0 to 0.0),
            Triple(-Double.MIN_VALUE, "-5e-324", -5.0 to -324.0),
            Triple(-1e-323, "-1e-323", -1.0 to -323.0),
            Triple(-0.1, "-0.1", -1.0 to -1.0),
            Triple(-1.0, "-1", -1.0 to 0.0),
            Triple(-1.1, "-1.1", -1.1 to 0.0),
            Triple(-10.0, "-10", -1.0 to 1.0),
            Triple(-1e10, "-1e10", -1.0 to 10.0),
            Triple(-1e308, "-1e308", -1.0 to 308.0),
            Triple(-Double.MAX_VALUE, "-1.7976931348623157e308", -1.7976931348623157 to 308.0),
        )
        for ((number, string, expected) in cases) {
            assertME(Decimal.fromDouble(number), expected.first, expected.second)
            assertME(Decimal.fromString(string), expected.first, expected.second)
        }
    }

    @Test
    fun nonFiniteValues() {
        assertME(Decimal.fromDouble(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY, 0.0)
        assertME(Decimal.fromDouble(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY, 0.0)
        assertNaNDecimal(Decimal.fromDouble(Double.NaN))
        assertNaNDecimal(Decimal.fromString("NaN"))
    }

    @Test
    fun bigStringValues() {
        assertME(Decimal.fromString("1.9e308"), 1.9, 308.0)
        assertME(Decimal.fromString("1e309"), 1.0, 309.0)
        assertME(Decimal.fromString("1e9000000000000000"), 1.0, 9000000000000000.0)
    }

    @Test
    fun cloneReturnsEquivalentValue() {
        val value = Decimal.fromDouble(6e9)
        assertME(value.clone(), 6.0, 9.0)
    }

    @Test
    fun zeroConstant() {
        assertME(Decimal.ZERO, 0.0, 0.0)
    }

    @Test
    fun fromMantissaExponentNormalizes() {
        assertME(Decimal.fromMantissaExponent(6.0, 9.0), 6.0, 9.0)
        assertME(Decimal.fromMantissaExponent(6000000000.0, 0.0), 6.0, 9.0)
        assertNaNDecimal(Decimal.fromMantissaExponent(Double.NaN, 9.0))
        assertNaNDecimal(Decimal.fromMantissaExponent(6.0, Double.NaN))
    }

    @Test
    fun fromMantissaExponentNoNormalizeSkipsNormalization() {
        assertME(Decimal.fromMantissaExponentNoNormalize(6.0, 9.0), 6.0, 9.0)
        assertME(Decimal.fromMantissaExponentNoNormalize(6000000000.0, 0.0), 6000000000.0, 0.0)

        val nanMantissa = Decimal.fromMantissaExponentNoNormalize(Double.NaN, 9.0)
        assertTrue(nanMantissa.mantissa.isNaN())
        assertEquals(9.0, nanMantissa.exponent)

        val nanExponent = Decimal.fromMantissaExponentNoNormalize(6.0, Double.NaN)
        assertEquals(6.0, nanExponent.mantissa)
        assertTrue(nanExponent.exponent.isNaN())
    }

    @Test
    fun fromDoubleSmokeTest() {
        assertME(Decimal.fromDouble(6e9), 6.0, 9.0)
    }

    @Test
    fun fromStringSmokeTest() {
        assertME(Decimal.fromString("6e9"), 6.0, 9.0)
    }
}
