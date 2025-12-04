package org.prime.tally.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DistributorRequest(
    val distributor_id: Int? = null,
    val distributor_name: String,
    val mobile_no: String,
    val password: String?=null,
    val ledger_name: String,
    val ledger_guid: String,
    val status: String?=null
)


@Serializable
data class DistributorResponse(
    val distributor_id: Int
)