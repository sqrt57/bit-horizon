package com.cosmic.bithorizon.math

/**
 * If you're willing to spend `resourcesAvailable` and want to buy something with
 * exponentially increasing cost each purchase (start at `priceStart`, multiply by
 * `priceRatio`, already own `currentOwned`), how much of it can you buy?
 * Adapted from Trimps source code.
 */
fun affordGeometricSeries(
    resourcesAvailable: Decimal,
    priceStart: Decimal,
    priceRatio: Decimal,
    currentOwned: Decimal,
): Decimal {
    val actualStart = priceStart.mul(priceRatio.pow(currentOwned))
    val value = resourcesAvailable.div(actualStart).mul(priceRatio.sub(1)).add(1).log10() / priceRatio.log10()
    return Decimal.fromDouble(value).floor()
}

fun affordGeometricSeries(
    resourcesAvailable: Decimal,
    priceStart: Decimal,
    priceRatio: Decimal,
    currentOwned: Double,
): Decimal = affordGeometricSeries(resourcesAvailable, priceStart, priceRatio, Decimal.fromDouble(currentOwned))

/**
 * How much resource would it cost to buy `numItems` items if you already have
 * `currentOwned`, the initial price is `priceStart`, and it multiplies by `priceRatio`
 * each purchase?
 */
fun sumGeometricSeries(
    numItems: Decimal,
    priceStart: Decimal,
    priceRatio: Decimal,
    currentOwned: Decimal,
): Decimal = priceStart.mul(priceRatio.pow(currentOwned))
    .mul(Decimal.ONE.sub(priceRatio.pow(numItems)))
    .div(Decimal.ONE.sub(priceRatio))

fun sumGeometricSeries(
    numItems: Double,
    priceStart: Decimal,
    priceRatio: Decimal,
    currentOwned: Decimal,
): Decimal = sumGeometricSeries(Decimal.fromDouble(numItems), priceStart, priceRatio, currentOwned)

/**
 * If you're willing to spend `resourcesAvailable` and want to buy something with
 * additively increasing cost each purchase (start at `priceStart`, add `priceAdd`,
 * already own `currentOwned`), how much of it can you buy?
 */
fun affordArithmeticSeries(
    resourcesAvailable: Decimal,
    priceStart: Decimal,
    priceAdd: Decimal,
    currentOwned: Decimal,
): Decimal {
    // n = (-(a-d/2) + sqrt((a-d/2)^2+2dS))/d, where a is actualStart, d is priceAdd,
    // S is resourcesAvailable. Floor it and you're done.
    val actualStart = priceStart.add(currentOwned.mul(priceAdd))
    val b = actualStart.sub(priceAdd.div(2.0))
    val b2 = b.pow(2)
    return b.neg().add(b2.add(priceAdd.mul(resourcesAvailable).mul(2.0)).sqrt()).div(priceAdd).floor()
}

/**
 * How much resource would it cost to buy `numItems` items if you already have
 * `currentOwned`, the initial price is `priceStart`, and it adds `priceAdd` each purchase?
 * Adapted from http://www.mathwords.com/a/arithmetic_series.htm
 */
fun sumArithmeticSeries(
    numItems: Decimal,
    priceStart: Decimal,
    priceAdd: Decimal,
    currentOwned: Decimal,
): Decimal {
    val actualStart = priceStart.add(currentOwned.mul(priceAdd))
    // (n/2)*(2*a+(n-1)*d)
    return numItems.div(2.0).mul(actualStart.mul(2.0).add(numItems.sub(1.0).mul(priceAdd)))
}

/**
 * When comparing two purchases that cost `cost` and increase your resource/sec by
 * `deltaRpS`, the lowest efficiency score is the better one to purchase.
 * From Frozen Cookies: http://cookieclicker.wikia.com/wiki/Frozen_Cookies_(JavaScript_Add-on)
 */
fun efficiencyOfPurchase(cost: Decimal, currentRpS: Decimal, deltaRpS: Decimal): Decimal =
    cost.div(currentRpS).add(cost.div(deltaRpS))
