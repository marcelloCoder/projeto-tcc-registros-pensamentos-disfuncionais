package br.com.mcoder.primeiroprojeto.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifications = AppNotifications(context)
        when (intent.action) {
            AppNotifications.ACTION_DAILY -> notifications.deliverDaily()
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> notifications.scheduleDaily()
        }
    }
}
