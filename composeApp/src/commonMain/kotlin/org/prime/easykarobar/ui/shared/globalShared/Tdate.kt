package org.prime.easykarobar.ui.shared.globalShared

fun Tdate(date: String): String {
    if (date.isBlank() || date == "-") return "-"
    val (yy, mm, dd) = if (date.contains("-")) {
        date.split("-")
    } else {
        // Fallback for unexpected formats
        return date
    }
    //pass the saved date format in this
    val dateFormat = getDateFormat()
    return when (dateFormat) {
        "dd-mm-yyyy" -> {
            "$dd-$mm-$yy"
        }

        "yyyy-mm-dd" -> {
            "$yy-$mm-$dd"
        }

        "yyyy-dd-mm" -> {
            "$yy-$dd-$mm"
        }

        "mm-dd-yyyy" -> {
            "$mm-$dd-$yy"
        }

        else -> {
            "$dd-$mm-$yy"
        }
    }
}

fun convertCouponDate(dateStr: String?): String? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        // Expected format: dd/MMM/yyyy (e.g., 25/Jan/2024)
        val parts = dateStr.split("/")
        if (parts.size != 3) return null

        val day = parts[0].padStart(2, '0')
        val monthStr = parts[1].lowercase()
        val year = parts[2]

        val month = when (monthStr) {
            "jan" -> "01"
            "feb" -> "02"
            "mar" -> "03"
            "apr" -> "04"
            "may" -> "05"
            "jun" -> "06"
            "jul" -> "07"
            "aug" -> "08"
            "sep" -> "09"
            "oct" -> "10"
            "nov" -> "11"
            "dec" -> "12"
            else -> "01"
        }
        "$year-$month-$day"
    } catch (e: Exception) {
        null
    }
}

fun extractNumericValue(input: String?): String {
    if (input.isNullOrBlank()) return "-"
    val colonIndex = input.indexOf(':')
    if (colonIndex == -1) return input.trim()
    val afterColon = input.substring(colonIndex + 1)
    val number = afterColon.trim().split("\\s".toRegex()).firstOrNull()
    return number?.takeIf { it.all { c -> c.isDigit() } } ?: afterColon.trim()
}