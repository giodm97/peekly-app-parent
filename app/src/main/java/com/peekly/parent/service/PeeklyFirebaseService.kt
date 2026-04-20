package com.peekly.parent.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.peekly.parent.MainActivity
import com.peekly.parent.PeeklyApplication
import com.peekly.parent.data.FcmTokenRequest
import com.peekly.parent.data.ParentDataStore
import com.peekly.parent.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PeeklyFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            ParentDataStore.storeFcmToken(applicationContext, token)
            val parentSub = ParentDataStore.getOrCreateParentSub(applicationContext)
            runCatching {
                ApiClient.instance.registerFcmToken(FcmTokenRequest(parentSub, token))
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data      = message.data
        val title     = data["title"]     ?: "Peekly"
        val body      = data["body"]      ?: "New digest available"
        val childName = data["childName"] ?: ""
        val childId   = data["childId"]?.toLongOrNull()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_CHILD_NAME, childName)
            childId?.let { putExtra(MainActivity.EXTRA_CHILD_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, PeeklyApplication.DIGEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
