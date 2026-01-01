package org.prime.tally.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
