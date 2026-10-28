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
    } catch (e: Throwable) {
        Log.e("FCMToken", "Error removing FCM token", e)
    }
}

actual suspend fun getFirebaseToken(): String? = suspendCancellableCoroutine { continuation ->
    try {
        val cached = SharedPrefs.FcmToken.get()
        if (!cached.isNullOrBlank()) {
            Log.d("FCMToken", "Using cached FCM token: $cached")
            continuation.resume(cached)
            return@suspendCancellableCoroutine
        }
        Log.d("FCMToken", "Requesting Firebase messaging token...")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val token = task.result
                Log.d("FCMToken", "Firebase token fetched successfully: $token")
                if (!token.isNullOrBlank()) {
                    SharedPrefs.FcmToken.save(token)
                }
                if (continuation.isActive) {
                    continuation.resume(token)
                }
            } else {
                Log.w("FCMToken", "Failed to fetch Firebase token", task.exception)
                val cachedToken = SharedPrefs.FcmToken.get()
                Log.d("FCMToken", "Using cached FCM token: $cachedToken")
                if (continuation.isActive) {
                    continuation.resume(cachedToken)
                }
            }
        }
    } catch (e: Throwable) {
        Log.e("FCMToken", "Exception getting Firebase token", e)
        val cached = SharedPrefs.FcmToken.get()
        if (continuation.isActive) {
            continuation.resume(cached)
        }
    }
}

actual suspend fun setupFcmSubscription(topic: String): String? {
    Log.d("FCMToken", "Setting up FCM subscription for topic: $topic")
    var token = SharedPrefs.FcmToken.get()
    if (token.isNullOrBlank()) {
        token = getFirebaseToken()
    }
    if (!token.isNullOrBlank()) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d("FCMToken", "Successfully subscribed to FCM topic: $topic")
        } catch (e: Throwable) {
            Log.e("FCMToken", "Failed to subscribe to FCM topic: $topic", e)
        }
    } else {
        Log.w("FCMToken", "Token is blank, cannot subscribe to topic: $topic")
    }
    return token
}

fun subscribeUserToFcmTopic() {
    val userId = SharedPrefs.User.get()?.ID?.toString()
    Log.d("FCMToken", "subscribeUserToFcmTopic called, userId: $userId")
    if (!userId.isNullOrBlank()) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            setupFcmSubscription(userId)
        }
    } else {
        Log.w("FCMToken", "UserId is blank, skipping FCM topic subscription")
    }
}
