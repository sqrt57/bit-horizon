package com.cosmic.bithorizon.math

internal fun repeatZeroes(count: Int): String = if (count <= 0) "" else "0".repeat(count)

internal fun trailZeroes(places: Int): String = if (places > 0) "." + repeatZeroes(places) else ""
