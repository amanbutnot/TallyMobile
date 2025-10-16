package org.prime.tally.data.expect

import kotlin.math.floor
import kotlin.math.pow

actual fun Double.formatToQtyDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = floor(this * factor) / factor
    return "%.${num}f".format(truncated)
}

actual fun Double.formatToAmtDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = floor(this * factor) / factor
    return "%.${num}f".format(truncated)
}