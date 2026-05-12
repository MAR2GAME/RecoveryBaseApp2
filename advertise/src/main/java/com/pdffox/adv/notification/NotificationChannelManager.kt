package com.pdffox.adv.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelManager {

    const val NORMAL_CHANNEL_ID = "normal_notification_channel"
    const val NORMAL_CHANNEL_NAME = "normal_notification_notice"

    fun createNormalChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NORMAL_CHANNEL_ID,
                NORMAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 200, 300)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
