package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val billing_guid: String, //LEDGER GUID
    val billing_name: String, //LEDGER NAME
    val remarks: String,
    val total_amt:String,
    val items: List<items>
)

@Serializable
data class items(
    val item_id: Int, //GUID
    val productName:String,
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
    val ID: Int,
    val hospital_id: Int,
    val AutoNo: Int,
    val OrderID: String,
    val order_date: String,
    val remarks: String,
    val cancellation_date: String?,
    val cancelled_by: String,
    val cancellation_remarks: String,
    val user_id: Int,
    val billing_name: String,
    val billing_mobile: String,
    val billing_address: String,
    val billing_city: String,
    val billing_state: String,
    val billing_country: String,
    val billing_pincode: String,
    val created_at: String,
    val items: List<OrderItem>,
    val order_status: OrderStatus = OrderStatus.Pending,
    val status_history: List<StatusHistory>

)

@Serializable
data class StatusHistory(
    val order_id: Int,
    val status: String,
    val remarks: String,
    val created_at: String
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
    val VoucherNumber: String,
)

enum class OrderStatus {
    Pending, Confirmed, InDispatched, Delivered, Cancelled
}