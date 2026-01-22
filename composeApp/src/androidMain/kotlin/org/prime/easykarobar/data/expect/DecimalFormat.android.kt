package org.prime.easykarobar.data.expect

actual fun Double.formatToQtyDec(num: Int): String {
    return this.toBigDecimal()
        .setScale(num, java.math.RoundingMode.DOWN)
        .toPlainString()
        //.replace(",","")
}

actual fun Double.formatToAmtDec(num: Int): String {
    val bd = this.toBigDecimal()
        .setScale(num, java.math.RoundingMode.DOWN)

    val formatter = java.text.NumberFormat.getNumberInstance(
        java.util.Locale("en", "IN")
    ).apply {
        minimumFractionDigits = num
        maximumFractionDigits = num
        isGroupingUsed = true
    }

    return formatter.format(bd)
        //.replace(",","")
}
