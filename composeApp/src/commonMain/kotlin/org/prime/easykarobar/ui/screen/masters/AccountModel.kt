package org.prime.easykarobar.ui.screen.masters

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