package com.cosmic.bithorizon.math

import kotlin.math.abs
import kotlin.math.floor

/**
 * JS `Math.round` always rounds `.5` toward +Infinity; `kotlin.math.round` rounds
 * half-to-even. Ports of break_infinity.js's rounding must use this, not `kotlin.math.round`.
 */
internal fun jsRound(x: Double): Double = floor(x + 0.5)

internal fun isInteger(x: Double): Boolean = x.isFinite() && floor(x) == x

internal fun isSafeInteger(x: Double): Boolean = isInteger(x) && abs(x) <= 9007199254740991.0 // 2^53 - 1

/**
 * The significant-digit sequence of a normalized mantissa (absolute value in `[1, 10)`),
 * with no decimal point and no spurious trailing zero (Double.toString() always appends
 * ".0" for whole values, e.g. "6.0" - that zero isn't a real significant digit).
 * The decimal point implicitly sits after the first character.
 */
private fun digitsOf(absMantissa: Double): String {
    val text = absMantissa.toString()
    val dot = text.indexOf('.')
    val intPart = text.substring(0, dot)
    val fracPart = text.substring(dot + 1)
    return if (fracPart == "0") intPart else intPart + fracPart
}

/**
 * Rounds a plain digit string (no sign, no point) to `keep` digits using round-half-up.
 * The result may be one character longer than `keep` if rounding carries out of the
 * front (e.g. "996" rounded to 2 digits is "100", not "10").
 */
private fun roundDigitString(digits: String, keep: Int): String {
    if (keep >= digits.length) return digits.padEnd(keep, '0')
    val roundUp = digits[keep] >= '5'
    val kept = digits.substring(0, keep)
    if (!roundUp) return kept

    val chars = kept.toCharArray()
    var i = chars.size - 1
    while (i >= 0 && chars[i] == '9') {
        chars[i] = '0'
        i--
    }
    return if (i < 0) {
        "1" + String(chars)
    } else {
        chars[i] = chars[i] + 1
        String(chars)
    }
}

/** Splits a normalized (mantissa, exponent) pair into plain-decimal integer/fraction digit runs. */
private fun splitDecimal(absMantissa: Double, exponent: Int): Pair<String, String> {
    val digits = digitsOf(absMantissa)
    val pointPos = 1 + exponent
    return when {
        pointPos <= 0 -> "0" to (repeatZeroes(-pointPos) + digits)
        pointPos >= digits.length -> (digits + repeatZeroes(pointPos - digits.length)) to ""
        else -> digits.substring(0, pointPos) to digits.substring(pointPos)
    }
}

/**
 * Natural (non-fixed-width, unrounded) plain-decimal rendering, e.g. `1.16`, `1000000`, `0.116`.
 * Only sensible for exponents small enough that the digit run stays reasonably sized
 * (callers guard the exponent range, matching upstream's toString() boundary).
 */
internal fun formatPlainDecimal(mantissa: Double, exponent: Int): String {
    val sign = if (mantissa < 0) "-" else ""
    val (intPart, fracPart) = splitDecimal(abs(mantissa), exponent)
    return sign + intPart + if (fracPart.isEmpty()) "" else ".$fracPart"
}

/** `mantissa`'s own natural digit rendering with no exponent shift, e.g. `6.0` -> `"6"`, `1.16` -> `"1.16"`. */
internal fun formatMantissaNatural(mantissa: Double): String {
    val sign = if (mantissa < 0) "-" else ""
    val digits = digitsOf(abs(mantissa))
    return sign + if (digits.length <= 1) digits else digits[0] + "." + digits.substring(1)
}

/** Fixed-width decimal rendering with exactly `places` digits after the point, rounded. */
internal fun formatFixed(mantissa: Double, exponent: Int, places: Int): String {
    val (intPart, fracPart) = splitDecimal(abs(mantissa), exponent)
    val fullDigits = intPart + fracPart
    val keep = intPart.length + places
    val rounded = roundDigitString(fullDigits, keep)
    val newIntLen = rounded.length - places
    val finalInt = rounded.substring(0, newIntLen)
    val finalFrac = rounded.substring(newIntLen)
    // A magnitude that rounds all the way down to zero (e.g. a tiny value the exponent
    // range can't usefully display at this many places) prints as unsigned "0", matching
    // upstream's underflow-to-zero behavior rather than showing a spurious "-0.00".
    val sign = if (mantissa < 0 && (finalInt + finalFrac).any { it != '0' }) "-" else ""
    return sign + finalInt + if (places > 0) ".$finalFrac" else ""
}

/** Exponential rendering with exactly `places` digits after the point, e.g. `1.20e+9`. */
internal fun formatExponential(mantissa: Double, exponent: Double, places: Int): String {
    val sign = if (mantissa < 0) "-" else ""
    val digits = digitsOf(abs(mantissa))
    val keep = 1 + places
    val rounded = roundDigitString(digits, keep)
    var expOut = exponent
    val digitsForBody = if (rounded.length > keep) {
        expOut += 1
        rounded.substring(0, keep)
    } else {
        rounded
    }
    val body = if (places > 0) digitsForBody[0] + "." + digitsForBody.substring(1) else digitsForBody
    return sign + body + "e" + (if (expOut >= 0) "+" else "") + expOut.toLong()
}

/** The mantissa value rounded to `places` decimal digits, e.g. `mantissa=1.2345, places=2` -> `1.23`. */
internal fun roundMantissa(mantissa: Double, places: Int): Double {
    if (mantissa == 0.0) return 0.0
    val sign = if (mantissa < 0) -1.0 else 1.0
    val digits = digitsOf(abs(mantissa))
    val keep = 1 + places
    val rounded = roundDigitString(digits, keep)
    val intLen = rounded.length - places
    val text = rounded.substring(0, intLen) + if (places > 0) "." + rounded.substring(intLen) else ""
    return sign * text.toDouble()
}
