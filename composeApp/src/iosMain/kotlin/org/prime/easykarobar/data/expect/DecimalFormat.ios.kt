package org.prime.easykarobar.data.expect

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import kotlin.math.floor
import kotlin.math.pow

actual fun Double.formatToQtyDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = floor(this * factor) / factor

    return NSString
        .stringWithFormat("%.${num}f", truncated)
        .toString()
       // .replace(",", "")
}


actual fun Double.formatToAmtDec(num: Int): String {
    val factor = 10.0.pow(num)
    val truncated = floor(this * factor) / factor

    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        locale = NSLocale(localeIdentifier = "en_IN")
        minimumFractionDigits = num.toULong()
        maximumFractionDigits = num.toULong()
        usesGroupingSeparator = true
    }

    return formatter
        .stringFromNumber(NSNumber(truncated))
//?.replace(",", "")
        ?: "0"
}
