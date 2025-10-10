package org.prime.tally.ui.shared.reportsShared

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

fun DueDays(endDate: String, dueDate: String): String {
    val due = LocalDate.parse(dueDate)
    val end = LocalDate.parse(endDate)
    return (end.toEpochDays() - due.toEpochDays()).toString()
}