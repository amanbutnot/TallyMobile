package org.prime.easykarobar.data.model.transactions

import kotlinx.serialization.Serializable
import org.prime.easykarobar.ui.screen.transactions.BillByBillModel

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
    val transportDetails: TransportDetails,
    val bills_collection: List<BillByBillModel>,
    val TransactionID: Int? = null, val total_amt: Double
)

@Serializable
data class SundryItem(
    val name: String,
    val amount: Double = 0.0,
    val rate: Double,
    val percentValue: Double,
    val srno: Int,
    val guid: String,
    val i1: Int,
    val i2: Int,
    val d2: Int,
)

@Serializable
data class TransportDetails(
    val transportName: String,
    val station: String,
    val gstNum: String,
    val vehicleNum: String,
    val pincode: String,
    val grDate: String,

    val SpartyName: String? = null,
    val SbillingShipping: Boolean? = null,
    val Saddress1: String? = null,
    val Saddress2: String? = null,
    val Saddress3: String? = null,
    val Saddress4: String? = null,
    val SshipState: String? = null,
    val SmobileNo: String? = null,
    val Semail: String? = null,
    val SitPan: String? = null,
    val SgstIn: String? = null,

    val OptionalField1: String? = null,
    val OptionalField2: String? = null,
    val OptionalField3: String? = null,
    val OptionalField4: String? = null,
    val OptionalField5: String? = null,
    val OptionalField6: String? = null,
    val OptionalField7: String? = null,
    val OptionalField8: String? = null,
    val OptionalField9: String? = null,
    val OptionalField10: String? = null,
    val OptionalField11: String? = null,
    val OptionalField12: String? = null,
    val OptionalField13: String? = null,
    val OptionalField14: String? = null,
    val OptionalField15: String? = null,
    val OptionalField16: String? = null,
    val OptionalField17: String? = null,
    val OptionalField18: String? = null,
    val OptionalField19: String? = null,
    val OptionalField20: String? = null,
    val Saadhar: String? = null,

    )


@Serializable
data class BillingItem(
    val product_id: String,
    val product_name: String,
    val CD: String,
    val quantity: Int,
    val list_price: Double,
    val discount_percent: Double? = null,
    val discount_amt: Double? = null,
    val tax_rate1: Double,
    val tax_rate2: Double,
    val taxable: Double,
    val gstAmt: Double,
    val net: Double,
    val guid: String,
    val itemdesc1: String? = null,
    val itemdesc2: String? = null,
    val itemdesc3: String? = null,
    val itemdesc4: String? = null,
    val itemdesc5: String? = null,
    val itemdesc6: String? = null,
    val itemdesc7: String? = null,
    val itemdesc8: String? = null,
    val itemdesc9: String? = null,
    val itemdesc10: String? = null,
    val itemdesc11: String? = null,
    val itemdesc12: String? = null,
    val itemdesc13: String? = null,
    val itemdesc14: String? = null,
    val itemdesc15: String? = null,
    val itemdesc16: String? = null,
    val itemdesc17: String? = null,
    val itemdesc18: String? = null,
    val itemdesc19: String? = null,
    val itemdesc20: String? = null,
    val additionalinfo: String? = null,
    val item_serial: List<String>,
    val conFactor: Double? = null,
    val conType: Double? = null,
    val selectedUnit: String? = null,
    val altQty: Double? = null,
    val item_parameter: List<String>
)


@Serializable
data class InventoryVoucherResponse(
    val VoucherNumber: String,
    val uniqueID: String,
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
    val OrderStatus: String,
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
    val billed_vchno: String,
    val billing_name: String,
    val billing_mobile: String,
    val billing_address: String,
    val billing_state: String,
    val billing_country: String,
    val created_at: String,
    val razorpay_payment_id: String,
    val other_info: TransportDetails? = null,
    val Others1: String,
    val Others2: String,
    val items: List<Item>,
    val taxType: Int,
    val uniqueID: String? = null,
    val Narration: String? = null,
    val item_serial: List<String> = emptyList(),
    val sundries: List<SundryItem>,
    val bills_collection: List<BillByBillModel>? = null
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
    val CD: String,
    //gst amount
    val taxamt1: String,
    val taxamt2: String,
    //net
    val total_amt: String,
    val taxType: Int,
    val itemdesc1: String? = null,
    val itemdesc2: String? = null,
    val itemdesc3: String? = null,
    val itemdesc4: String? = null,
    val itemdesc5: String? = null,
    val itemdesc6: String? = null,
    val itemdesc7: String? = null,
    val itemdesc8: String? = null,
    val itemdesc9: String? = null,
    val itemdesc10: String? = null,
    val itemdesc11: String? = null,
    val itemdesc12: String? = null,
    val itemdesc13: String? = null,
    val itemdesc14: String? = null,
    val itemdesc15: String? = null,
    val itemdesc16: String? = null,
    val itemdesc17: String? = null,
    val itemdesc18: String? = null,
    val itemdesc19: String? = null,
    val itemdesc20: String? = null,
    val item_serial: List<String> = emptyList(),
    val item_parameter: List<String> = emptyList(),
    val additionalinfo: String? = null,
    val conFactor: Double? = null,
    val conType: Double? = null,
    val selectedUnit: String? = null,
    val altQty: Double? = null
)
