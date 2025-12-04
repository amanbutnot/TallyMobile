package org.prime.tally.data.model.transactions

import kotlinx.serialization.Serializable

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
    val Narration: String
)

@Serializable
data class TranResponse(
    val VoucherNumber: String?=null,
    val TransactionID: Int?=null,
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
    val C1: String,
    val C2: String,
    val C3: String,
    val C4: String,
    val D1: Double,
    val D2: Double,
    val D3: Double,
    val D4: Double,
    val Narration: String,
    val CreatedAt: String
)