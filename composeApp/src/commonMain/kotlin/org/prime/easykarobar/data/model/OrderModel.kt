package org.prime.easykarobar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.model.transactions.SundryItem

@Serializable
data class CreateOrderRequest(
    val billing_guid: String, //LEDGER GUID
    val billing_name: String, //LEDGER NAME
    val remarks: String,
    val total_amt: String,
    val items: List<items>,
    val sundries: List<SundryItem> = emptyList()
)

@Serializable
data class items(
    val item_id: Int, //GUID
    val productName: String,
    val quantity: Double,
    val price: Double,
    val discount_percent: Double,
    val tax_amount: Double,
    val net_amount: Double
)

@Serializable
data class CreateOrderResponse(
    val VoucherNumber: String
)

@Serializable
data class Order(
    val id: Int,
    val store_id: Int,
    val user_id: Int,
    val VchType: Int,
    val AutoVchNo: Int,
    val order_no: String,
    val total_amount: String,
    val status: String,
    val OrderStatus: ORDERSTATUS,
    val status_billed: ORDERSTATUS,
    val billing_guid: String?=null,
    val billing_name: String,
    val billing_mobile: String,
    val billing_address: String,
    val billing_state: String,
    val billing_country: String,
    val created_at: String,
    val razorpay_payment_id: String,
    val Others1: String,
    val Others2: String,
    val Remarks: String,
    val items: List<OrderItemList>,
    val sundries: List<SundryItem> = emptyList(),
    val status_history: List<StatusHistory>,
    val cancellation_date: String? = null,
    val cancelled_by: String? = null,
    val cancellation_remarks: String? = null
)


@Serializable
data class StatusHistory(
    val order_id: Int? = null,
    val status: String? = null,
    val remarks: String? = null,
    val created_at: String? = null
)

@Serializable
data class OrderItemList(
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
    val item_amount: String,
    val tax_rate1: String,
    val tax_rate2: String,
    val taxamt1: String,
    val taxamt2: String,
    val total_amt: String,
    val taxType: Int,
    val UnitCode: String,
    val UnitName: String
)

@Serializable
data class OrderItem(
    val ID: Int,
    val OrderID: String,
    val line_no: Int,
    val item_id: Int,
    val category_id: Int,
    val unit_id: Int,
    val quantity: String,
    val rate: String,
    val price: String,
    val mrp: String,
    val discount_percent: String,
    val discounted_price: String?,
    val packing_charges: String,
    val amount: String,
    val tax_amount: String,
    val net_amount: String,
    val created_at: String,
    val profile_picture: String?
)

@Serializable
data class CancelOrderRequest(
    val order_id: Int,
    val cancellation_remarks: String
)

@Serializable
data class CancelOrderResponse(
    val order_id: String,
)

@Serializable
data class UpdateOrderStatusRequest(
    val order_id: Int,
    val status: String,
    val remarks: String = ""
)

enum class ORDERSTATUS {
    @SerialName("Pending")
    Pending,

    @SerialName("Confirmed")
    Confirmed,

    @SerialName("Ready for pickup")
    ReadyForPickup,

    @SerialName("Out for delivery")
    OutForDelivery,

    @SerialName("Delivered")
    Delivered,

    @SerialName("Cancelled")
    Cancelled,

    @SerialName("Billed")
    Billed,

    @SerialName("Posted")
    Posted;

    fun displayName(): String {
        return when (this) {
            ReadyForPickup -> "Ready for pickup"
            OutForDelivery -> "Out for delivery"
            else -> this.name
        }
    }
}
