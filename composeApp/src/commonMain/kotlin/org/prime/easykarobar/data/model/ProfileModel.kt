package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DriveTokenResponse(
    val access_token: String,
    val expires_in: Int
)