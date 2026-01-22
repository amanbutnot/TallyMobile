package org.prime.easykarobar.ui.shared.globalShared

fun Tdate(date: String): String {
    val (yy, mm, dd) = date.split("-")
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