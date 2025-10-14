package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder

fun CompanyName(): String {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.companyNameQuery().executeAsOne()
    return com.T1.toString()
}