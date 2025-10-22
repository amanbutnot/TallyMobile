package org.prime.tally.ui.shared.globalShared

import org.prime.tally.data.expect.DatabaseHolder

fun CompanyName(): String {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.companyNameQuery().executeAsOne()
    return com.T1.toString()
}

fun getDateFormat():String{
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.getCompanyInformation().executeAsOne()
    return com.T8.toString()
}

fun getQtyDecimal(): Int {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.getCompanyInformation().executeAsOne()
    return com.D3?.toInt() ?: 1
}
fun getAmtDecimal(): Int {
    val db = DatabaseHolder.instance
    val com = db.companyInformationQueries.getCompanyInformation().executeAsOne()
    return com.D4?.toInt() ?: 1
}
