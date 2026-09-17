package org.prime.easykarobar.data.utils

import org.prime.easykarobar.data.expect.DatabaseHolder

object SaleOrderConfig {
    val db = DatabaseHolder.instance
    val config = db.companyConfigurationQueries
    fun discountAsPer(vchType: Int): Boolean {
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D1 != 1.0)
    }

    fun showAddItemInSaleOrder(vchType: Int): Boolean {
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D2 != 1.0)
    }

    fun showTransport(vchType: Int): Boolean {
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D3 != 1.0)
    }

    fun showBilling(vchType: Int): Boolean {
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D4 != 1.0)
    }

    fun showOptionalFields(vchType: Int): Boolean {
        return (config.saleOrderConfig("9999_$vchType").executeAsOneOrNull()?.D5 != 1.0)
    }

}