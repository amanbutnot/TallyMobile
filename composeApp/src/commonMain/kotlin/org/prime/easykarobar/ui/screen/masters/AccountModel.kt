package org.prime.easykarobar.ui.screen.masters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountModel(
    val name: String? = null,
    val alias: String? = null,
    val printName: String? = null,
    val parentGroupName: String? = null,
    val parentGroupGuid: String? = null,
    val openingBalance: String? = null,
    val drCr: String? = null,
    val gstNo: String? = null,
    val itPan: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val addressLine4: String? = null,
    val country: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val station: String? = null,
    val mobileNo: String? = null,
    val email: String? = null,
    val whatsappNo: String? = null,
    val maintainBillByBill: Int? = null,
    val saleCreditDays: String? = null,
    val ledger_guid: String? = null,
    val purchaseCreditDays: String? = null,
)

@Serializable
data class DeleteAccountRequest(
    val ledger_guid: String,
    val remarks: String
)

@Serializable
data class CreateItemResponse(

    val name: String,
    val alias: String,
    val printName: String,

    val parentGroup: String,
    val parentGroupGuid: String,

    val mainUnit: String,
    val mainUnitGuid: String,

    val altUnit: String,
    val altUnitGuid: String,

    val altSameAsMain: Boolean?=null,
    val conType: String,
    val conFactor: Double,

    val taxCategoryName: String,
    val taxCategoryGuid: String,

    val opQty: String,
    val opQtyAlt: String,
    val opAmount: String,

    val salePrice: String,
    val salePriceAlt: String,
    val purchPrice: String,
    val purchPriceAlt: String,
    val mrp: String,
    val minSalePrice: String,
    val selfValPrice: String,

    val saleDiscount: String,
    val purchDiscount: String,

    val desc1: String,
    val desc2: String,
    val desc3: String,
    val desc4: String,

    @SerialName("product_guid")
    val productGuid: Long?=null,

    @SerialName("store_id")
    val storeId: Long
)

@Serializable
data class ProductItemResponse(

    val id: Long,

    @SerialName("store_id")
    val storeId: Long,

    val name: String,
    val alias: String,
    val printName: String,

    val parentGroup: String,
    val parentGroupGuid: String,

    val mainUnit: String,
    val mainUnitGuid: String,

    val altUnit: String,
    val altUnitGuid: String,

    val altSameAsMain: Boolean, // 0/1

    val conType: String,
    val conFactor: String,

    val taxCategoryName: String,
    val taxCategoryGuid: String,

    val opQty: String,
    val opQtyAlt: String,
    val opAmount: String,

    val salePrice: String,
    val salePriceAlt: String?=null,
    val purchPrice: String?=null,
    val purchPriceAlt: String?=null,
    val mrp: String,
    val minSalePrice: String,
    val selfValPrice: String,

    val saleDiscount: String,
    val purchDiscount: String,

    val desc1: String,
    val desc2: String,
    val desc3: String,
    val desc4: String,

    val created_at: String,
    val updated_at: String,

    val status: String,

    val cancelled_remarks: String?,
    val cancelled_on: String?,

    val GUID: Long
)