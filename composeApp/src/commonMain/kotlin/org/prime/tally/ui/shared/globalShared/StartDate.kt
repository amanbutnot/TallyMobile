package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder

fun StartDate(): String {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.startDateQuery().executeAsOne()
    return com.T2.toString()
}