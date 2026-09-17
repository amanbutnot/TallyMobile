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
fun sendNotification(context: Context, title: String, body: String) {
    createChannel(context)
    val builder =
        NotificationCompat.Builder(context, "default").setContentTitle(title).setContentText(body)
            .setAutoCancel(true)
            .setSmallIcon(org.prime.easykarobar.R.drawable.ic_launcher)
            .build()
    val notifManager = NotificationManagerCompat.from(context)
    notifManager.notify(1001, builder)
}