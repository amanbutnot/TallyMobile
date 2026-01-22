package org.prime.easykarobar.data.utils

import kotlin.math.pow
import kotlin.math.round



//TODO: later make it such that it takes the decimal integer from the table

fun Double.formatDecimals(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor
    val integerPart = rounded.toLong()
    var fractionalPart = ((rounded - integerPart) * factor).toLong()

    val fractionalStr = fractionalPart.toString().padStart(decimals, '0')

    return "$integerPart.$fractionalStr"
}