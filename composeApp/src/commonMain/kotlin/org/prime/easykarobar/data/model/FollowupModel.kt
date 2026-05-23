package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostFollowup(
    val ActCode: String,
    val nextfollowup: String,
    val status: String,
    val Remarks: String
)

@Serializable
data class PostFollowupResponse(
    val ID: Int,
    val followupdate: String
)