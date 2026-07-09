package com.cosmic.bithorizon.math

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalCompareTest {

    private fun t(left: Double, right: Double, expected: Int) {
        val l = Decimal.fromDouble(left)
        val r = Decimal.fromDouble(right)
        assertEquals(expected, l.cmp(r), "cmp($left, $right)")
        assertEquals(expected, l.compare(r), "compare($left, $right)")
    }

    @Test
    fun zeroAndEqualMagnitudes() {
        t(0.0, 0.0, 0)
        t(0.0, 116.0, -1)
        t(116.0, 0.0, 1)
        t(116.0, 116.0, 0)
        t(-116.0, -116.0, 0)
    }

    @Test
    fun crossSignAndCrossExponent() {
        t(116.0, -10.0, 1)
        t(-10.0, 116.0, -1)
        t(-100.0, -10.0, -1)
        t(-10.0, -100.0, 1)
        t(5.0, 74900.0, -1)
        t(74900.0, 5.0, 1)
        t(7.0, 1e280, -1)
        t(1e280, 7.0, 1)
    }

    @Test
    fun extremeExponentGap() {
        val small = Decimal.fromDouble(1e300)
        val huge = Decimal.fromString("1e10500")
        assertEquals(-1, small.cmp(huge))
        assertEquals(1, huge.cmp(small))
    }

    @Test
    fun infinityAndNaN() {
        val posInf = Decimal.fromDouble(Double.POSITIVE_INFINITY)
        val negInf = Decimal.fromDouble(Double.NEGATIVE_INFINITY)
        val one = Decimal.fromDouble(1.0)
        val nan = Decimal.fromDouble(Double.NaN)

        assertEquals(1, posInf.cmp(one))
        assertEquals(-1, one.cmp(posInf))
        assertEquals(0, posInf.cmp(posInf))
        assertEquals(-1, negInf.cmp(one))
        assertEquals(1, one.cmp(negInf))
        assertEquals(0, negInf.cmp(negInf))
        assertEquals(1, one.cmp(nan))
        assertEquals(-1, nan.cmp(one))
        assertEquals(0, nan.cmp(nan))
    }

    @Test
    fun ltAndLessThanAgree() {
        val values = listOf(-1e280, -100.0, -10.0, 0.0, 5.0, 74900.0, 1e280)
        for (a in values) {
            for (b in values) {
                val da = Decimal.fromDouble(a)
                val db = Decimal.fromDouble(b)
                assertEquals(da.lt(db), da.lessThan(db), "lt/lessThan disagree for $a, $b")
                assertEquals(da.gt(db), da.greaterThan(db), "gt/greaterThan disagree for $a, $b")
                assertEquals(da.lte(db), da.lessThanOrEqualTo(db), "lte/lessThanOrEqualTo disagree for $a, $b")
                assertEquals(da.gte(db), da.greaterThanOrEqualTo(db), "gte/greaterThanOrEqualTo disagree for $a, $b")
            }
        }
    }
}
