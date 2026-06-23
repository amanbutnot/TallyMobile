package org.prime.easykarobar.ui.shared.globalShared

fun getCategoryImage(storeId: String, guid: String): String {
    return "https://easykarobar.in/assets/category/${storeId}_${guid}.webp"
}

fun getProductImage(storeId: String, guid: String): String {
    return "https://images.easykarobar.in/stores/${storeId}/t/$storeId$guid.webp?v=104"
}

fun String.toValidUrl(): String {
    val value = trim()

    return when {
        value.startsWith("http://", true) ||
                value.startsWith("https://", true) -> value

        else -> "https://$value"
    }
}