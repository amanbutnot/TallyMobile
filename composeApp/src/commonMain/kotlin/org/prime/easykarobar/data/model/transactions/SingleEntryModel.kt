package org.prime.easykarobar.data.model.transactions

import kotlinx.serialization.Serializable
import org.prime.easykarobar.ui.screen.transactions.BillByBillModel

@Serializable
data class TranRequest(
    val TransactionID: Int? = null,
    val VchType: Int,
    val TranDate: String,
    val CM1: String,
    val CM2: String,
    val CM3: String,
    val CM4: String,
    val C1: String,
    val C2: String,
    val C3: String,
    val C4: String,
    val D1: Double,
    val D2: Double,
    val D3: Double,
    val D4: Double,
    val Narration: String,
    val bills_collection: List<BillByBillModel>,
    val pdcDate: String? = null,
    val pdcType: String,
    val instrumentName: String,
    val instrumentNumber: String
)

@Serializable
data class TranResponse(
    val VoucherNumber: String? = null,
    val TransactionID: Int? = null,
    val uniqueID: String? = null,
)

@Serializable
data class TranListRequest(
    val VchType: Int,
    val StartDate: String,
    val EndDate: String
)

@Serializable
data class TranListResponse(
    val TransactionID: Int,
    val CardID: String,
    val TranDate: String,
    val VchType: Int,
    val VchNo: String,
    val AutoVchNo: Int,
    val CM1: String,
    val CM2: String,
    val CM3: String,
    val CM4: String,
    val billed_vchno: String,
    val pdcDate: String? = null,
    val pdcType: String? = null,
    val C1: String,
    val C2: String,
    val C3: String,
    val C4: String,
    val D1: Double,
    val D2: Double,
    val D3: Double,
    val D4: Double,
    val Narration: String,
    val instrumentName: String?=null,
    val instrumentNumber: String?=null,
    val CreatedAt: String,
    val status: String? = null,
    val bills_collection: List<BillByBillModel>
)