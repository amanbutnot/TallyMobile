package org.prime.tally.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val statuscode: Int,
    val message: String,
    val data: T? = null
)
