package com.peekly.parent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class PeeklyApplication : Application() {

    companion object {
        const val DIGEST_CHANNEL_ID = "digest_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DIGEST_CHANNEL_ID,
            "Daily Digest",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when your child's daily digest is ready"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
