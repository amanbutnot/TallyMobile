package org.prime.tally.data.expect

import platform.Foundation.stringWithFormat
import kotlin.math.floor
import kotlin.math.pow

import platform.Foundation.NSString

actual fun Double.formatToQtyDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = kotlin.math.floor(this * factor) / factor
    return NSString.stringWithFormat("%.${num}f", truncated).toString()
}

actual fun Double.formatToAmtDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = kotlin.math.floor(this * factor) / factor
    return NSString.stringWithFormat("%.${num}f", truncated).toString()
}