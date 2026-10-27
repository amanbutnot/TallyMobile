package org.prime.easykarobar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.subscribeUserToFcmTopic
import org.prime.easykarobar.data.utils.SharedPrefs

class MyFirebaseApplication : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Firebase token is $token")
        if (token.isNotBlank()) {
            SharedPrefs.FcmToken.save(token)
            subscribeUserToFcmTopic()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        println("========== FCM onMessageReceived START ==========")

        println("FCM: Message received")
        println("FCM: Message ID = ${message.messageId}")
        println("FCM: From = ${message.from}")
        println("FCM: Sent time = ${message.sentTime}")
        println("FCM: TTL = ${message.ttl}")
        println("FCM: Collapse key = ${message.collapseKey}")

        println("FCM: Data = ${message.data}")
        println("FCM: Notification = ${message.notification}")

        val notification = message.notification

        val title = notification?.title
            ?: message.data["title"]
            ?: message.data["heading"]
            ?: ""
        val body = notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: message.data["description"]
            ?: ""

        if (title.isBlank() && body.isBlank()) {
            println("FCM: Both notification and data payloads are empty/null")
            println("========== FCM onMessageReceived END ==========")
            return
        }

        println("FCM: Title = $title")
        println("FCM: Body = $body")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            println("FCM: Android 13+ detected")
            println("FCM: Checking POST_NOTIFICATIONS permission")

            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            println("FCM: POST_NOTIFICATIONS permission result = $permission")

            if (permission != PackageManager.PERMISSION_GRANTED) {
                println("FCM: ❌ Notification permission DENIED")
                println("FCM: Returning without sending notification")
                println("========== FCM onMessageReceived END ==========")
                return
            }

            println("FCM: ✅ Notification permission GRANTED")
        } else {
            println("FCM: Android version < 13")
            println("FCM: Notification permission check not required")
        }

        val timestamp = System.currentTimeMillis()

        try {
            val db = DatabaseHolder.instance
            db.notificationHistoryQueries.insertNotification(
                title,
                body,
                timestamp
            )
            println("FCM: Saved notification to database")
        } catch (e: Exception) {
            println("FCM: Error saving notification to database: ${e.message}")
        }

        try {
            SharedPrefs.NotificationsList.add(
                SharedPrefs.NotificationsList.SavedNotificationItem(
                    title = title,
                    body = body,
                    timestamp = timestamp
                )
            )
            println("FCM: Saved notification to SharedPrefs list")
        } catch (e: Exception) {
            println("FCM: Error saving notification to SharedPrefs: ${e.message}")
        }

        println("FCM: Calling sendNotification()")
        println("FCM: sendNotification title = $title")
        println("FCM: sendNotification body = $body")

        sendNotification(
            this,
            title,
            body
        )

        println("FCM: sendNotification() completed")

        println("========== FCM onMessageReceived END ==========")
    }
}