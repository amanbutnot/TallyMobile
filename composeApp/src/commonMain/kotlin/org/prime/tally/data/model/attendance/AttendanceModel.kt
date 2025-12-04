package org.prime.tally.data.model.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRequest(
    val LoginID: String,
    val TranType: Int,
    val RecType: Int,
    val C1: String? = null,
    val C2: String,
    val C3: String,
    val C4: String,
    val C5: String,
)


@Serializable
data class AttendanceResponse(
    val LocationID: Int,
    val LocationDateTime: String
)

@Serializable
data class AttendanceListResponse(
    val LocationDateTime: String,
    val LocationID: Int,
    val RecType: Int,
    val C1: String?=null,
    val C2: String,
    val C3: String,
    val C4: String,
    val C5: String
)

@Serializable
data class AttendanceListRequest(
    val VchType: Int,
    val StartDate: String,
    val EndDate: String,

)
