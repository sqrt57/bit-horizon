package com.cosmic.bithorizon.math

/**
 * If two exponents are more than this many digits apart, adding them together
 * is pointless (the smaller one is entirely swallowed by floating-point rounding)
 * so [Decimal.add] just returns the larger operand.
 */
internal const val MAX_SIGNIFICANT_DIGITS = 17

/**
 * Highest exponent magnitude a [Decimal] can hold. Chosen so it round-trips exactly
 * as a Double (it's under 2^53), keeping exponent arithmetic exact.
 */
internal const val EXP_LIMIT = 9e15

/** Largest exponent that can appear in a Double, though not all mantissas are valid there. */
internal const val NUMBER_EXP_MAX = 308

/** Smallest exponent that can appear in a Double, though not all mantissas are valid there. */
internal const val NUMBER_EXP_MIN = -324

/** Relative tolerance used to compensate for floating-point error when converting to Double. */
internal const val ROUND_TOLERANCE = 1e-10
