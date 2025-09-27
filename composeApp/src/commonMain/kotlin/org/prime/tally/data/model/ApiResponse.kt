package org.prime.tally.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val statuscode: Int,
    val message: String,
    val data: T? = null
)
