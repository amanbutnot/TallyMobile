package org.prime.tally.data.model.transactions

import kotlinx.serialization.Serializable

@Serializable
data class InventoryVoucherRequest(
    val billing_guid: String,
    val vch_type: Int,
    val billing_name: String,
    val TranDate: String,
    val billing_mobile: String,
    val billing_state: String,
    val billing_country: String,
    val billing_address: String,
    val taxType: Int,
    val items: List<BillingItem>,
    val sundries: List<SundryItem>,
    val Narration: String,
    val TransactionID: Int? = null
)

@Serializable
data class SundryItem(
    val name: String,
    val amount: Double = 0.0,
    val rate: Double,
    val srno: Int,
    val guid: String,
    val i1: Int,
    val i2: Int,
    val d2: Int,
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
    val tax_rate2: Double,
    val taxable: Double,
    val gstAmt: Double,
    val net: Double,
    val guid: String
)


@Serializable
data class InventoryVoucherResponse(
    val VoucherNumber: String
)

@Serializable
data class InventoryListResponse(
    val id: Int,
    val store_id: Int,
    val user_id: Int,
    val VchType: Int,
    val order_no: String,
    val total_amount: String,
    val status: String,
    val status_billed: String,
    val billing_guid: String,
    val billing_name: String,
    val billing_mobile: String,
    val billing_address: String,
    val billing_state: String,
    val billing_country: String,
    val created_at: String,
    val razorpay_payment_id: String,
    val Others1: String,
    val Others2: String
)

@Serializable
data class InventoryListRequest(
    val VchType: Int,
    val StartDate: String,
    val EndDate: String
)

@Serializable
data class InventoryItemResponse(
    val id: Int,
    val store_id: Int,
    val user_id: Int,
    val VchType: Int,
    val AutoVchNo: Int,
    val order_no: String,
    val total_amount: String,
    val status: String,
    val status_billed: String,
    val billing_guid: String,
    val billing_name: String,
    val billing_mobile: String,
    val billing_address: String,
    val billing_state: String,
    val billing_country: String,
    val created_at: String,
    val razorpay_payment_id: String,
    val Others1: String,
    val Others2: String,
    val items: List<Item>, val taxType: Int
)

@Serializable
data class Item(
    val id: Int,
    val store_id: Int,
    val order_id: Int,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val price: String,
    val list_price: String,
    val discount_percent: String,
    val discount_amt: String,
    val nett_price: String,
    //taxable
    val item_amount: String,
    //gst
    val tax_rate1: String,
    val tax_rate2: String,
    //gst amount
    val taxamt1: String,
    val taxamt2: String,
    //net
    val total_amt: String,
    val taxType: Int
)