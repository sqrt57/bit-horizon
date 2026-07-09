package com.cosmic.bithorizon.math

import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * An arbitrary-magnitude number represented as `mantissa * 10^exponent`, where `mantissa`
 * has absolute value in `[1, 10)` (or is exactly 0) and `exponent` is an integral value in
 * `[-EXP_LIMIT, EXP_LIMIT]`. A pure-Kotlin port of break_infinity.js
 * (https://github.com/Patashu/break_infinity.js) - see design/backlog.md for why this
 * exists instead of a general-purpose BigDecimal.
 *
 * Immutable: every operation returns a new instance rather than mutating this one.
 */
class Decimal private constructor(val mantissa: Double, val exponent: Double) : Comparable<Decimal> {

    val m: Double get() = mantissa
    val e: Double get() = exponent
    val s: Int get() = sgn()

    fun clone(): Decimal = this

    // region sign/abs/neg

    fun abs(): Decimal = fromMantissaExponentNoNormalize(mantissa.absoluteValue, exponent)

    fun neg(): Decimal = fromMantissaExponentNoNormalize(-mantissa, exponent)
    fun negate(): Decimal = neg()
    fun negated(): Decimal = neg()
    operator fun unaryMinus(): Decimal = neg()

    fun sgn(): Int = sign(mantissa).toInt()
    fun sign(): Int = sgn()

    // endregion

    // region rounding

    fun round(): Decimal = when {
        exponent < -1 -> ZERO
        exponent < MAX_SIGNIFICANT_DIGITS -> fromDouble(jsRound(toDouble()))
        else -> this
    }

    fun floor(): Decimal = when {
        !isFinite() -> this
        exponent < -1 -> if (mantissa >= 0) ZERO else MINUS_ONE
        exponent < MAX_SIGNIFICANT_DIGITS -> fromDouble(floor(toDouble()))
        else -> this
    }

    fun ceil(): Decimal = when {
        exponent < -1 -> if (mantissa > 0) ONE else ZERO
        exponent < MAX_SIGNIFICANT_DIGITS -> fromDouble(ceil(toDouble()))
        else -> this
    }

    fun trunc(): Decimal = when {
        exponent < 0 -> ZERO
        exponent < MAX_SIGNIFICANT_DIGITS -> fromDouble(truncate(toDouble()))
        else -> this
    }

    // endregion

    // region add/sub/mul/div/recip

    fun add(other: Decimal): Decimal {
        if (!isFinite()) return this
        if (!other.isFinite()) return other
        if (mantissa == 0.0) return other
        if (other.mantissa == 0.0) return this

        val bigger: Decimal
        val smaller: Decimal
        if (exponent >= other.exponent) {
            bigger = this; smaller = other
        } else {
            bigger = other; smaller = this
        }

        if (bigger.exponent - smaller.exponent > MAX_SIGNIFICANT_DIGITS) return bigger

        // Adding numbers that were once integers but scaled down is imprecise otherwise.
        val newMantissa = jsRound(
            1e14 * bigger.mantissa +
                1e14 * smaller.mantissa * powerOf10((smaller.exponent - bigger.exponent).toInt()),
        )
        return fromMantissaExponent(newMantissa, bigger.exponent - 14)
    }
    fun add(other: Double): Decimal = add(fromDouble(other))
    fun add(other: Int): Decimal = add(other.toDouble())
    operator fun plus(other: Decimal): Decimal = add(other)
    operator fun plus(other: Double): Decimal = add(other)
    operator fun plus(other: Int): Decimal = add(other)

    fun sub(other: Decimal): Decimal = add(other.neg())
    fun sub(other: Double): Decimal = sub(fromDouble(other))
    fun sub(other: Int): Decimal = sub(other.toDouble())
    fun subtract(other: Decimal): Decimal = sub(other)
    operator fun minus(other: Decimal): Decimal = sub(other)
    operator fun minus(other: Double): Decimal = sub(other)
    operator fun minus(other: Int): Decimal = sub(other)

    fun mul(other: Decimal): Decimal = fromMantissaExponent(mantissa * other.mantissa, exponent + other.exponent)
    fun mul(other: Double): Decimal =
        // Avoids an extra Decimal conversion when possible; the mantissa is within (-10, 10)
        // so anything short of MAX/10 can be safely multiplied in directly.
        if (other < 1e307 && other > -1e307) {
            fromMantissaExponent(mantissa * other, exponent)
        } else {
            fromMantissaExponent(mantissa * 1e-307 * other, exponent + 307)
        }
    fun mul(other: Int): Decimal = mul(other.toDouble())
    fun multiply(other: Decimal): Decimal = mul(other)
    operator fun times(other: Decimal): Decimal = mul(other)
    operator fun times(other: Double): Decimal = mul(other)
    operator fun times(other: Int): Decimal = mul(other)

    operator fun div(other: Decimal): Decimal = mul(other.recip())
    operator fun div(other: Double): Decimal = mul(fromDouble(other).recip())
    operator fun div(other: Int): Decimal = div(other.toDouble())
    fun divide(other: Decimal): Decimal = div(other)
    fun divideBy(other: Decimal): Decimal = div(other)
    fun dividedBy(other: Decimal): Decimal = div(other)

    fun recip(): Decimal = fromMantissaExponent(1 / mantissa, -exponent)
    fun reciprocal(): Decimal = recip()
    fun reciprocate(): Decimal = recip()

    // endregion

    // region comparisons

    fun cmp(other: Decimal): Int {
        if (isNaN()) return if (other.isNaN()) 0 else -1
        if (other.isNaN()) return 1
        return compareRaw(this, other)
    }
    fun cmp(other: Double): Int = cmp(fromDouble(other))
    fun cmp(other: Int): Int = cmp(other.toDouble())
    fun compare(other: Decimal): Int = cmp(other)
    override fun compareTo(other: Decimal): Int = cmp(other)

    fun eq(other: Decimal): Boolean = exponent == other.exponent && mantissa == other.mantissa
    fun eq(other: Double): Boolean = eq(fromDouble(other))
    fun eq(other: Int): Boolean = eq(other.toDouble())
    fun equals(other: Decimal): Boolean = eq(other)

    fun neq(other: Decimal): Boolean = !eq(other)
    fun neq(other: Double): Boolean = neq(fromDouble(other))
    fun neq(other: Int): Boolean = neq(other.toDouble())
    fun notEquals(other: Decimal): Boolean = neq(other)

    fun lt(other: Decimal): Boolean {
        if (mantissa == 0.0) return other.mantissa > 0
        if (other.mantissa == 0.0) return mantissa <= 0
        if (exponent == other.exponent) return mantissa < other.mantissa
        return if (mantissa > 0) other.mantissa > 0 && exponent < other.exponent
        else other.mantissa > 0 || exponent > other.exponent
    }
    fun lt(other: Double): Boolean = lt(fromDouble(other))
    fun lt(other: Int): Boolean = lt(other.toDouble())
    fun lessThan(other: Decimal): Boolean {
        if (isNaN() || other.isNaN()) return false
        return compareRaw(this, other) < 0
    }

    fun lte(other: Decimal): Boolean = !gt(other)
    fun lte(other: Double): Boolean = lte(fromDouble(other))
    fun lte(other: Int): Boolean = lte(other.toDouble())
    fun lessThanOrEqualTo(other: Decimal): Boolean {
        if (isNaN() || other.isNaN()) return false
        return compareRaw(this, other) < 1
    }

    fun gt(other: Decimal): Boolean {
        if (mantissa == 0.0) return other.mantissa < 0
        if (other.mantissa == 0.0) return mantissa > 0
        if (exponent == other.exponent) return mantissa > other.mantissa
        return if (mantissa > 0) other.mantissa < 0 || exponent > other.exponent
        else other.mantissa < 0 && exponent < other.exponent
    }
    fun gt(other: Double): Boolean = gt(fromDouble(other))
    fun gt(other: Int): Boolean = gt(other.toDouble())
    fun greaterThan(other: Decimal): Boolean {
        if (isNaN() || other.isNaN()) return false
        return compareRaw(this, other) > 0
    }

    fun gte(other: Decimal): Boolean = !lt(other)
    fun gte(other: Double): Boolean = gte(fromDouble(other))
    fun gte(other: Int): Boolean = gte(other.toDouble())
    fun greaterThanOrEqualTo(other: Decimal): Boolean {
        if (isNaN() || other.isNaN()) return false
        return compareRaw(this, other) > -1
    }

    // endregion

    // region max/min/clamp

    fun max(other: Decimal): Decimal = if (lt(other)) other else this
    fun max(other: Double): Decimal = max(fromDouble(other))
    fun min(other: Decimal): Decimal = if (gt(other)) other else this
    fun min(other: Double): Decimal = min(fromDouble(other))

    fun clamp(minValue: Decimal, maxValue: Decimal): Decimal = max(minValue).min(maxValue)
    fun clampMin(minValue: Decimal): Decimal = max(minValue)
    fun clampMax(maxValue: Decimal): Decimal = min(maxValue)

    // endregion

    // region tolerance comparisons

    /** Relative tolerance: multiplied by the larger operand's magnitude before comparing. */
    fun eqTolerance(other: Decimal, tolerance: Decimal): Boolean =
        sub(other).abs().lte(abs().max(other.abs()).mul(tolerance))
    fun eqTolerance(other: Decimal, tolerance: Double): Boolean = eqTolerance(other, fromDouble(tolerance))

    fun cmpTolerance(other: Decimal, tolerance: Decimal): Int = if (eqTolerance(other, tolerance)) 0 else cmp(other)
    fun cmpTolerance(other: Decimal, tolerance: Double): Int = cmpTolerance(other, fromDouble(tolerance))

    fun neqTolerance(other: Decimal, tolerance: Decimal): Boolean = !eqTolerance(other, tolerance)
    fun ltTolerance(other: Decimal, tolerance: Decimal): Boolean = !eqTolerance(other, tolerance) && lt(other)
    fun lteTolerance(other: Decimal, tolerance: Decimal): Boolean = eqTolerance(other, tolerance) || lt(other)
    fun gtTolerance(other: Decimal, tolerance: Decimal): Boolean = !eqTolerance(other, tolerance) && gt(other)
    fun gteTolerance(other: Decimal, tolerance: Decimal): Boolean = eqTolerance(other, tolerance) || gt(other)

    // endregion

    // region logarithms/exponentials

    fun log10(): Double = exponent + log10(mantissa)
    fun absLog10(): Double = exponent + log10(mantissa.absoluteValue)
    fun pLog10(): Double = if (mantissa <= 0 || exponent < 0) 0.0 else log10()

    fun log(base: Double): Double = (ln(10.0) / ln(base)) * log10()
    fun logarithm(base: Double): Double = log(base)
    fun log2(): Double = 3.321928094887362 * log10()
    fun ln(): Double = 2.302585092994045 * log10()

    fun pow(value: Double): Decimal {
        if (mantissa == 0.0) return this

        // Fast track: if (exponent*value) is an integer and mantissa^value fits in a Double.
        val temp = exponent * value
        if (isSafeInteger(temp)) {
            val fastMantissa = mantissa.pow(value)
            if (fastMantissa.isFinite() && fastMantissa != 0.0) return fromMantissaExponent(fastMantissa, temp)
        }

        // Same speed and usually more accurate.
        val newExponent = truncate(temp)
        val residue = temp - newExponent
        val newMantissa = 10.0.pow(value * log10(mantissa) + residue)
        if (newMantissa.isFinite() && newMantissa != 0.0) return fromMantissaExponent(newMantissa, newExponent)

        val result = pow10(value * absLog10())
        if (sgn() == -1) {
            val oddness = (value % 2).absoluteValue
            return when (oddness) {
                1.0 -> result.neg()
                0.0 -> result
                else -> NaN
            }
        }
        return result
    }
    fun pow(value: Decimal): Decimal = pow(value.toDouble())
    fun pow(value: Int): Decimal = pow(value.toDouble())
    fun powBase(base: Decimal): Decimal = base.pow(toDouble())
    fun powBase(base: Double): Decimal = fromDouble(base).pow(toDouble())

    fun exp(): Decimal {
        val x = toDouble()
        return if (x > -706 && x < 709) fromDouble(exp(x)) else fromDouble(E).pow(x)
    }

    // endregion

    // region powers/roots

    fun sqr(): Decimal = fromMantissaExponent(mantissa.pow(2), exponent * 2)

    fun sqrt(): Decimal {
        if (mantissa < 0) return NaN
        return if (exponent % 2 != 0.0) {
            fromMantissaExponent(sqrt(mantissa) * 3.16227766016838, floor(exponent / 2))
        } else {
            fromMantissaExponent(sqrt(mantissa), floor(exponent / 2))
        }
    }

    fun cube(): Decimal = fromMantissaExponent(mantissa.pow(3), exponent * 3)

    fun cbrt(): Decimal {
        val cbrtSign = if (mantissa < 0) -1.0 else 1.0
        val newMantissa = cbrtSign * mantissa.absoluteValue.pow(1.0 / 3.0)
        val mod = exponent % 3
        return when {
            mod == 1.0 || mod == -2.0 -> fromMantissaExponent(newMantissa * 2.154434690031883, floor(exponent / 3))
            mod != 0.0 -> fromMantissaExponent(newMantissa * 4.641588833612778, floor(exponent / 3))
            else -> fromMantissaExponent(newMantissa, floor(exponent / 3))
        }
    }

    // endregion

    fun dp(): Double {
        if (!isFinite()) return Double.NaN
        if (exponent >= MAX_SIGNIFICANT_DIGITS) return 0.0

        var places = -exponent
        var scale = 1.0
        while ((jsRound(mantissa * scale) / scale - mantissa).absoluteValue > ROUND_TOLERANCE) {
            scale *= 10
            places += 1
        }
        return if (places > 0) places else 0.0
    }
    fun decimalPlaces(): Double = dp()

    // region formatting

    fun toDouble(): Double {
        if (!isFinite()) return mantissa
        if (exponent > NUMBER_EXP_MAX) return if (mantissa > 0) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
        if (exponent < NUMBER_EXP_MIN) return 0.0
        if (exponent == NUMBER_EXP_MIN.toDouble()) return if (mantissa > 0) 5e-324 else -5e-324

        val result = mantissa * powerOf10(exponent.toInt())
        if (!result.isFinite() || exponent < 0) return result
        val resultRounded = jsRound(result)
        return if ((resultRounded - result).absoluteValue < ROUND_TOLERANCE) resultRounded else result
    }

    fun mantissaWithDecimalPlaces(places: Int): Double {
        if (!isFinite()) return mantissa
        if (mantissa == 0.0) return 0.0
        return roundMantissa(mantissa, places)
    }

    override fun toString(): String {
        if (!isFinite()) return mantissa.toString()
        if (exponent <= -EXP_LIMIT || mantissa == 0.0) return "0"
        if (exponent < 21 && exponent > -7) return formatPlainDecimal(mantissa, exponent.toInt())
        return formatMantissaNatural(mantissa) + "e" + (if (exponent >= 0) "+" else "") + exponent.toLong()
    }

    fun toExponential(places: Int): String {
        if (!isFinite()) return mantissa.toString()
        if (exponent <= -EXP_LIMIT || mantissa == 0.0) return "0" + trailZeroes(places) + "e+0"
        return formatExponential(mantissa, exponent, places)
    }

    fun toFixed(places: Int): String {
        if (!isFinite()) return mantissa.toString()
        if (exponent <= -EXP_LIMIT || mantissa == 0.0) return "0" + trailZeroes(places)
        return formatFixed(mantissa, exponent.toInt(), places)
    }

    fun toPrecision(places: Int): String {
        if (exponent <= -7) return toExponential(places - 1)
        if (places > exponent) return toFixed((places - exponent - 1).toInt())
        return toExponential(places - 1)
    }

    fun toStringWithDecimalPlaces(places: Int): String = toExponential(places)

    // endregion

    // region extras

    fun factorial(): Decimal {
        // Stirling's approximation: https://en.wikipedia.org/wiki/Stirling%27s_approximation
        val n = toDouble() + 1
        val base = (n / E) * sqrt(n * sinh(1 / n) + 1 / (810 * n.pow(6)))
        return fromDouble(base).pow(n).mul(sqrt(2 * PI / n))
    }

    fun sinh(): Decimal = exp().sub(neg().exp()).div(2.0)
    fun cosh(): Decimal = exp().add(neg().exp()).div(2.0)
    fun tanh(): Decimal = sinh().div(cosh())
    fun asinh(): Double = add(sqr().add(ONE).sqrt()).ln()
    fun acosh(): Double = add(sqr().sub(ONE).sqrt()).ln()
    fun atanh(): Double {
        if (abs().gte(ONE)) return Double.NaN
        return add(ONE).div(ONE.sub(this)).ln() / 2
    }

    /** Joke function from Realm Grinder. */
    fun ascensionPenalty(ascensions: Double): Decimal = if (ascensions == 0.0) this else pow(10.0.pow(-ascensions))
    fun ascensionPenalty(ascensions: Int): Decimal = ascensionPenalty(ascensions.toDouble())

    /** Joke function from Cookie Clicker. It's 'egg'. */
    fun egg(): Decimal = add(9)

    // endregion

    // region state checks

    fun isFinite(): Boolean = mantissa.isFinite()
    fun isNaN(): Boolean = mantissa.isNaN()
    fun isPositiveInfinity(): Boolean = mantissa == Double.POSITIVE_INFINITY
    fun isNegativeInfinity(): Boolean = mantissa == Double.NEGATIVE_INFINITY

    // endregion

    override fun equals(other: Any?): Boolean {
        if (other !is Decimal) return false
        return mantissa == other.mantissa && exponent == other.exponent
    }

    override fun hashCode(): Int {
        // Canonicalize -0.0 to 0.0: `-0.0 == 0.0` is true above (IEEE `==`), so the
        // hashCode contract requires them to hash the same, but Double.hashCode() (total
        // order) would otherwise disagree.
        val canonicalMantissa = if (mantissa == 0.0) 0.0 else mantissa
        val canonicalExponent = if (exponent == 0.0) 0.0 else exponent
        return 31 * canonicalMantissa.hashCode() + canonicalExponent.hashCode()
    }

    companion object {
        val ZERO: Decimal = Decimal(0.0, 0.0)
        val ONE: Decimal = Decimal(1.0, 0.0)
        val MINUS_ONE: Decimal = Decimal(-1.0, 0.0)
        val MAX_VALUE: Decimal = Decimal(1.0, EXP_LIMIT)
        val MIN_VALUE: Decimal = Decimal(1.0, -EXP_LIMIT)
        val NaN: Decimal = Decimal(Double.NaN, 0.0)
        val POSITIVE_INFINITY: Decimal = Decimal(Double.POSITIVE_INFINITY, 0.0)
        val NEGATIVE_INFINITY: Decimal = Decimal(Double.NEGATIVE_INFINITY, 0.0)

        fun fromMantissaExponentNoNormalize(mantissa: Double, exponent: Double): Decimal = Decimal(mantissa, exponent)

        fun fromMantissaExponent(mantissa: Double, exponent: Double): Decimal {
            if (!mantissa.isFinite() || !exponent.isFinite()) return NaN
            return normalize(mantissa, exponent)
        }

        private fun normalize(mantissa: Double, exponent: Double): Decimal {
            if (mantissa >= 1 && mantissa < 10) return Decimal(mantissa, exponent)
            if (mantissa == 0.0) return Decimal(0.0, 0.0)

            val tempExponent = floor(log10(mantissa.absoluteValue))
            val newMantissa = if (tempExponent == NUMBER_EXP_MIN.toDouble()) {
                mantissa * 10 / 1e-323
            } else {
                mantissa / powerOf10(tempExponent.toInt())
            }
            return Decimal(newMantissa, exponent + tempExponent)
        }

        fun fromDouble(value: Double): Decimal {
            if (!value.isFinite()) return fromMantissaExponentNoNormalize(value, 0.0)
            if (value == 0.0) return ZERO

            val exp = floor(log10(value.absoluteValue))
            val mant = if (exp == NUMBER_EXP_MIN.toDouble()) value * 10 / 1e-323 else value / powerOf10(exp.toInt())
            return normalize(mant, exp)
        }
        fun fromInt(value: Int): Decimal = fromDouble(value.toDouble())

        fun fromString(value: String): Decimal {
            val lower = value.lowercase()
            if (lower == "nan") return NaN

            val eIndex = lower.indexOf('e')
            if (eIndex != -1) {
                val mantissaValue = lower.substring(0, eIndex).toDoubleOrNull() ?: 1.0
                val exponentValue = lower.substring(eIndex + 1).toDoubleOrNull() ?: return NaN
                return fromMantissaExponent(mantissaValue, exponentValue)
            }

            val parsed = lower.toDoubleOrNull()
                ?: throw IllegalArgumentException("[DecimalError] Invalid argument: $value")
            return fromDouble(parsed)
        }

        val NUMBER_MAX_VALUE: Decimal = fromDouble(Double.MAX_VALUE)
        val NUMBER_MIN_VALUE: Decimal = fromDouble(Double.MIN_VALUE)

        fun pow10(value: Double): Decimal =
            if (isInteger(value)) {
                fromMantissaExponentNoNormalize(1.0, value)
            } else {
                fromMantissaExponent(10.0.pow(value % 1), truncate(value))
            }

        /** Mirrors math.ts's `cmp()` - the sign/exponent/mantissa comparison shared by cmp/lessThan/greaterThan. */
        private fun compareRaw(left: Decimal, right: Decimal): Int {
            if (left.mantissa == 0.0) {
                return when {
                    right.mantissa == 0.0 -> 0
                    right.mantissa < 0 -> 1
                    else -> -1
                }
            }
            if (right.mantissa == 0.0) {
                return if (left.mantissa < 0) -1 else 1
            }
            if (left.mantissa > 0) {
                return when {
                    right.mantissa < 0 -> 1
                    left.exponent > right.exponent -> 1
                    left.exponent < right.exponent -> -1
                    left.mantissa > right.mantissa -> 1
                    left.mantissa < right.mantissa -> -1
                    else -> 0
                }
            }
            return when {
                right.mantissa > 0 -> -1
                left.exponent > right.exponent -> -1
                left.exponent < right.exponent -> 1
                left.mantissa > right.mantissa -> 1
                left.mantissa < right.mantissa -> -1
                else -> 0
            }
        }
    }
}
