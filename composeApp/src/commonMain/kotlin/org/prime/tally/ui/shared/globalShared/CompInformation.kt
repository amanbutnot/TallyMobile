package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder

fun CompanyName(): String {
    val db = DatabaseHolder.instance
    val row = db.companyInformationQueries.companyNameQuery().executeAsOneOrNull()
    return row?.T1 ?: ""
}

fun getDateFormat(): String {
    val db = DatabaseHolder.instance
    val row = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    println(row?.T8.toString())
    return row?.T8 ?: "dd-mm-yyyy"
}

fun getQtyDecimal(): Int {
    val db = DatabaseHolder.instance
    val row = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    return row?.D3?.toInt() ?: 1
}

fun getAmtDecimal(): Int {
    val db = DatabaseHolder.instance
    val row = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    return row?.D4?.toInt() ?: 1
}
