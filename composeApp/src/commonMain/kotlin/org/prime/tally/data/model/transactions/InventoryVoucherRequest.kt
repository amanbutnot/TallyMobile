package org.prime.tally.data.model.transactions

import kotlinx.serialization.Serializable
import org.prime.tally.ui.screen.transactions.sale.SundryItem

@Serializable
data class InventoryVoucherRequest(
    val billing_guid: String,
    val vch_type: Int,
    val billing_name: String,
    val billing_mobile: String,
    val billing_state: String,
    val billing_country: String,
    val billing_address: String,
    val taxType: Int,
    val items: List<BillingItem>,
    val sundries: List<SundryItem>
)

@Serializable
data class BillingItem(
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val list_price: Double,
    val discount_percent: Double? = null,
    val discount_amt: Double? = null,
    val tax_rate1: Double,
    val tax_rate2: Double
)


@Serializable
data class InventoryVoucherResponse(
    val VoucherNumber: String
)