package org.prime.easykarobar

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


fun createChannel(context: Context) {
    val ch = NotificationChannel(
        "default", "default_channel", NotificationManager.IMPORTANCE_DEFAULT
    )
    val notifManager = context.getSystemService(NotificationManager::class.java)
    notifManager.createNotificationChannel(ch)
}


@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun sendNotification(context: Context, title: String, body: String, imageUrl: String? = null) {
    createChannel(context)
    val builder = NotificationCompat.Builder(context, "default")
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .setSmallIcon(org.prime.easykarobar.R.drawable.ic_launcher)

    if (!imageUrl.isNullOrBlank()) {
        try {
            val url = java.net.URL(imageUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val bitmap = android.graphics.BitmapFactory.decodeStream(input)
            if (bitmap != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as android.graphics.Bitmap?)
                )
            }
        } catch (e: Exception) {
            println("Error loading notification image for status bar: ${e.message}")
        }
    }

    val notification = builder.build()
    val notifManager = NotificationManagerCompat.from(context)
    notifManager.notify(1001, notification)
}
