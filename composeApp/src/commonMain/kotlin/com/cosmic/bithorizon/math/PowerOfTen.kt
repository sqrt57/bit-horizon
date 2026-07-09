package com.cosmic.bithorizon.math

// Math.pow(10, exponent)-equivalents are slightly inaccurate for large |exponent|,
// so this lookup table trades a bit of memory for exact powers of ten. Covers the
// full range a Double's exponent can take: [NUMBER_EXP_MIN + 1, NUMBER_EXP_MAX].
private const val INDEX_OF_ZERO = -NUMBER_EXP_MIN - 1

private val powersOf10: DoubleArray = DoubleArray(NUMBER_EXP_MAX - NUMBER_EXP_MIN) { i ->
    ("1e" + (i - INDEX_OF_ZERO)).toDouble()
}

internal fun powerOf10(power: Int): Double = powersOf10[power + INDEX_OF_ZERO]
