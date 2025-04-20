package com.example.projectmanager.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projectmanager.R
import com.example.projectmanager.activities.LoginActivity
import com.example.projectmanager.activities.MainActivity
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * this is for notification service
 */

class MyFirebaseMessagingService : FirebaseMessagingService(){
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.e("MyFirebaseMessaging","From: ${message.from}")
        println("********** MyFirebaseMessaging onMessageReceived ${message.data}")

        message.data.isNotEmpty().let {
            Log.e("MyFirebaseMessaging", "Data: ${message.data}")

            val title = message.data[Constants.FCM_KEY_TITLE]
            val messageBody = message.data[Constants.FCM_KEY_MESSAGE]
            sendNotification(title!!, messageBody!!)

        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("MyFirebaseMessaging","New Token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token:String?){
        val sharedPreferences =
            this.getSharedPreferences(Constants.PROJMANAGER_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }
    private fun sendNotification(title:String, message:String){
        Log.e("MyFirebaseMessaging","sendNotification called")
        val intent= if (FirestoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,LoginActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP //if many activities active, this will put this on top of stack and finish below activities
        )
        //As we cannot launch our app from another app just by intent, we use pending intent
        val pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)
        val channelId=this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(
            this,channelId).setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel=NotificationChannel(channelId,
            "Channel ProjectManger title",
            NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }
}