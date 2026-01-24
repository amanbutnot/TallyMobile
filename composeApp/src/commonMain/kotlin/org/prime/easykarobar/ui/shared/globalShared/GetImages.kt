package org.prime.easykarobar.ui.shared.globalShared

fun getCategoryImage(storeId: String, guid: String): String {
    return "https://easykarobar.in/assets/category/${storeId}_${guid}.webp"
}

fun getProductImage(storeId: String, guid: String): String {
    return "https://images.easykarobar.in/stores/${storeId}/t/$storeId$guid.webp?v=104"
}
