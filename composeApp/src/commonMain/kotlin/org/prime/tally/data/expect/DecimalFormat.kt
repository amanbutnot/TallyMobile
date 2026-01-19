package org.prime.tally.data.expect

import org.prime.tally.ui.shared.globalShared.getAmtDecimal
import org.prime.tally.ui.shared.globalShared.getQtyDecimal

expect fun Double.formatToQtyDec(num: Int = getQtyDecimal()): String
expect fun Double.formatToAmtDec(num: Int = getAmtDecimal()): String

fun String.stringToDouble(): Double {
    return this.replace(",", "").toDouble()
}



