package it.am.gpsmodule.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.am.gpsmodule.R

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        var title:String="Notifica Aski"
        var message:String = "Descrizione"
        var image:Int = R.drawable.car_icon

        fun setNotification(title: String, message: String, image: Int) {
            this.title = title
            this.message = message
            this.image = image
        }


        fun isAlarmSet(context: Context, alarmId: Int): Boolean {
            val alarmIntent = Intent(context, this::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, PendingIntent.FLAG_NO_CREATE)
            return pendingIntent != null
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val notificationUtils = NotificationUtils(context)
        val notification = notificationUtils.getNotificationBuilder(title, message, image).build()
        notificationUtils.getManager().notify(150, notification)
    }

}