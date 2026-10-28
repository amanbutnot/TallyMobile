package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class OnClickAction(val value: String, val displayName: String) {
    NOTHING("nothing", "Nothing"),
    OPEN_ITEM("open item", "Open Item"),
    OPEN_ITEM_CATEGORY("open item category", "Open Item Category"),
    OPEN_BRAND("open brand", "Open Brand"),
    OPEN_ITEM_SUBCATEGORY("open item subcategory", "Open Item Subcategory");

    companion object {
        fun fromValue(value: String): OnClickAction {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: NOTHING
        }
    }
}

@Serializable
data class NotificationPayload(
    val title: String = "",
    val contentType: String = "desc",
    val description: String? = null,
    val hasImage: Boolean = false,
    val imageUrl: String? = null,
    val onClickAction: String = "nothing",
    val entityGuid: String? = null,
    val entityName: String? = null,
    val timestamp: Long = 0L
)
