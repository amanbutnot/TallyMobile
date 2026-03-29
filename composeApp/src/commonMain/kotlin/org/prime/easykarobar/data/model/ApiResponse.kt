package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.prime.easykarobar.ui.screen.masters.AccountModel
import org.prime.easykarobar.ui.screen.masters.ProductItemResponse
import org.prime.easykarobar.ui.screen.transactions.BillByBillModel

@Serializable
data class ApiResponse<T>(
    val statuscode: Int,
    val message: String,
    val data: T? = null
)

@Serializable
data class LoginApiWrapper(
    val statuscode: Int,
    val message: String,
    val data: JsonElement
)

@Serializable
data class AccountItemResponse(
    val statuscode: Int,
    val message: String,
    val data: List<AccountModel>? = null,
    val data_items: List<ProductItemResponse>? = null,
    val data_bills: List<BillByBillModel>? = null,
)