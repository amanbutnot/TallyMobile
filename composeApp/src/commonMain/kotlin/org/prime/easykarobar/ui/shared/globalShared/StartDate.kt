package org.prime.easykarobar.ui.shared.globalShared

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.prime.easykarobar.data.expect.DatabaseHolder

fun StartDate(): String {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.startDateQuery().executeAsOne()
    return com.T2.toString()
}

fun PreviousDate(): String {
    val dateObj = LocalDate.parse(StartDate())
    val prevDate = dateObj.minus(DatePeriod(days = 1))
    return prevDate.toString()
}