package org.prime.easykarobar.data.expect

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.prime.easykarobar.data.utils.SharedPrefs
import kotlin.coroutines.resume

actual fun removeFirebaseToken() {
    try {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCMToken", "FCM token deleted successfully")
            } else {
                Log.w("FCMToken", "Failed to delete FCM token", task.exception)
            }
        }
    } catch (e: Exception) {
        Log.e("FCMToken", "Error removing FCM token", e)
    }
}

actual suspend fun getFirebaseToken(): String? = suspendCancellableCoroutine { continuation ->
    try {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val token = task.result
                if (!token.isNullOrBlank()) {
                    SharedPrefs.FcmToken.save(token)
                    subscribeUserToFcmTopic()
                }
                if (continuation.isActive) {
                    continuation.resume(token)
                }
            } else {
                val cached = SharedPrefs.FcmToken.get()
                if (continuation.isActive) {
                    continuation.resume(cached)
                }
            }
        }
    } catch (e: Exception) {
        val cached = SharedPrefs.FcmToken.get()
        if (continuation.isActive) {
            continuation.resume(cached)
        }
    }
}

actual suspend fun setupFcmSubscription(topic: String): String? {
    val token = getFirebaseToken()
    if (!token.isNullOrBlank()) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
        } catch (e: Exception) {
            // Handle subscription error
        }
    }
    return token
}

fun subscribeUserToFcmTopic() {
    val userId = SharedPrefs.User.get()?.ID?.toString()
    if (!userId.isNullOrBlank()) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            setupFcmSubscription(userId)
        }
    }
}
