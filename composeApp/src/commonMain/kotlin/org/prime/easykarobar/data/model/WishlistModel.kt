package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WishlistRequest(
    val mobile_no: String,
    val item_name: String? = null,
    val group_name: String? = null
)

@Serializable
data class WishlistItem(
    val id: Int? = null,
    val mobile_no: String? = null,
    val item_name: String? = null,
    val group_name: String? = null,
    val created_at: String? = null,
    val store_id: Int? = null
)
