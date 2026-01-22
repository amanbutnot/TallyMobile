package org.prime.easykarobar.ui.shared.reportsShared

import kotlinx.datetime.LocalDate

fun DueDays(endDate: String, dueDate: String): String {
    val due = LocalDate.parse(dueDate)
    val end = LocalDate.parse(endDate)
    return (end.toEpochDays() - due.toEpochDays()).toString()
}