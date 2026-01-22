package org.prime.easykarobar.data.expect

import org.prime.easykarobar.ui.shared.globalShared.getAmtDecimal
import org.prime.easykarobar.ui.shared.globalShared.getQtyDecimal

expect fun Double.formatToQtyDec(num: Int = getQtyDecimal()): String
expect fun Double.formatToAmtDec(num: Int = getAmtDecimal()): String

fun String.stringToDouble(): Double {
    return this.replace(",", "").toDouble()
}



