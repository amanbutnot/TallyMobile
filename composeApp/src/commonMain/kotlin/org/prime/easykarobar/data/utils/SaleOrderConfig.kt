package org.prime.easykarobar.data.utils

import org.prime.easykarobar.data.expect.DatabaseHolder

object SaleOrderConfig {
    val db = DatabaseHolder.instance
    val config = db.companyConfigurationQueries
    fun discountAsPer(): Boolean {
        return (config.saleOrderConfig().executeAsOneOrNull()?.D1 == 1.0)
    }

    fun showAddItemInSaleOrder(): Boolean {
        return (config.saleOrderConfig().executeAsOneOrNull()?.D2 == 1.0)
    }

    fun showTransport(): Boolean {
        return (config.saleOrderConfig().executeAsOneOrNull()?.D3 == 1.0)
    }

    fun showBilling(): Boolean {
        return (config.saleOrderConfig().executeAsOneOrNull()?.D4 == 1.0)
    }

    fun showOptionalFields(): Boolean {
        return (config.saleOrderConfig().executeAsOneOrNull()?.D5 == 1.0)
    }

}