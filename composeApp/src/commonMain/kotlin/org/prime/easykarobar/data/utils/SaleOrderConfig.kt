package org.prime.easykarobar.data.utils

import org.prime.easykarobar.data.expect.DatabaseHolder

object SaleOrderConfig {
    val db = DatabaseHolder.instance
    val config = db.companyConfigurationQueries

    private fun isTargetUser(): Boolean {
        return SharedPrefs.User.get()?.ID?.toString() == "10371"
    }

    fun discountAsPer(vchType: Int): Boolean {
        if (!isTargetUser()) return false
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D1 != 1.0)
    }

    fun showAddItemInSaleOrder(vchType: Int): Boolean {
        if (!isTargetUser()) return true
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D2 != 1.0)
    }

    fun showTransport(vchType: Int): Boolean {
        if (!isTargetUser()) return true
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D3 != 1.0)
    }

    fun showBilling(vchType: Int): Boolean {
        if (!isTargetUser()) return true
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D4 != 1.0)
    }

    fun showOptionalFields(vchType: Int): Boolean {
        if (!isTargetUser()) return true
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D5 != 1.0)
    }

}
