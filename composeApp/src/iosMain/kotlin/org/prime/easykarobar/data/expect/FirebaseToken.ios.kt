package org.prime.easykarobar.data.expect

import org.prime.easykarobar.data.utils.SharedPrefs
import platform.Foundation.NSNotificationCenter

actual fun removeFirebaseToken() {
    try {
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = "RemoveFirebaseTokenNotification",
            `object` = null,
        )
    } catch (e: Exception) {
        println("Error posting RemoveFirebaseTokenNotification: ${e.message}")
    }
}

actual suspend fun getFirebaseToken(): String? {
    return SharedPrefs.FcmToken.get()
}

actual suspend fun setupFcmSubscription(topic: String): String? {
    TODO("Not yet implemented")
}