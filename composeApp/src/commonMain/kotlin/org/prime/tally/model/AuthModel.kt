package org.prime.tally.model

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val Username: String,
    val Password: String
)
@Serializable
data class LoginResponse(
    val ID: Int,
    val FirstName: String,
    val LastName: String,
    val Mobile: String,
    val Email: String,
    val Gender: String,
    val AddressLine1: String,
    val AddressLine2: String,
    val City: String,
    val State: String,
    val Country: String,
    val Pincode: String,
    val CreatedAt: String,
    val UpdatedAt: String,
    val C1: String,
    val C2: String,
    val C3: String,
    val C4: String,
    val C5: String,
    val C6: String,
    val C7: String,
    val C8: String,
    val C9: String,
    val C10: String,
    val token: String,
    val token_expiry: String
)