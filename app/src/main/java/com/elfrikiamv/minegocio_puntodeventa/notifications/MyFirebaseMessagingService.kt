package com.elfrikiamv.minegocio_puntodeventa.notifications

// MyFirebaseMessagingService.kt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elfrikiamv.minegocio_puntodeventa.MainActivity
import com.elfrikiamv.minegocio_puntodeventa.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //println()
        Log.d(
            "MyFirebaseMessagingService",
            "Titulo recibido: ${message.notification?.title.toString()}"
        )
        Log.d(
            "MyFirebaseMessagingService",
            "Cuerpo recibido: ${message.notification?.body.toString()}"
        )

        message.notification.let {
            val title = message.notification?.title ?: getString(R.string.app_name)
            val body = message.notification?.body
            if (body != null) {
                sendNotification(title, body)
            }
        }
    }

    private fun sendNotification(messageTitle: String, messageBody: String) {
        val requestCode = 0
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle(messageTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            //.setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}