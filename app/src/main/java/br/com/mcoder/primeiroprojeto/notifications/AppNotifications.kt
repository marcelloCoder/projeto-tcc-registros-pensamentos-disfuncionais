package br.com.mcoder.primeiroprojeto.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.mcoder.primeiroprojeto.MainActivity
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.data.AppStorage
import br.com.mcoder.primeiroprojeto.data.AuthRepository
import br.com.mcoder.primeiroprojeto.model.UserAccount
import java.time.LocalDate
import java.time.ZonedDateTime

class AppNotifications(
    context: Context,
    private val currentUser: () -> UserAccount? = {
        AuthRepository(AppStorage(context.applicationContext)).getCurrentUser()
    },
    preferencesName: String = "mind_check_notifications"
) {
    private val context = context.applicationContext
    private val manager = this.context.getSystemService(NotificationManager::class.java)
    private val alarms = this.context.getSystemService(AlarmManager::class.java)
    private val preferences = this.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    var permissionRequested: Boolean
        get() = preferences.getBoolean("permission_requested", false)
        set(value) { preferences.edit().putBoolean("permission_requested", value).apply() }

    fun createChannels() {
        manager.createNotificationChannels(listOf(
            NotificationChannel(DAILY_CHANNEL, context.getString(R.string.notification_daily_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_daily_description)
            },
            NotificationChannel(SAVED_CHANNEL, context.getString(R.string.notification_saved_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_saved_description)
            }
        ))
    }

    fun canNotify(channel: String): Boolean =
        (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            manager.areNotificationsEnabled() && manager.getNotificationChannel(channel)?.importance != NotificationManager.IMPORTANCE_NONE

    fun scheduleDaily(now: ZonedDateTime = ZonedDateTime.now()) {
        createChannels()
        if (currentUser() == null || !canNotify(DAILY_CHANNEL)) {
            alarms.cancel(dailyIntent())
            return
        }
        val lastSent = preferences.getString("daily_last_date", null)?.let(LocalDate::parse)
        val alreadyScheduledToday = preferences.getString("scheduled_date", null) == now.toLocalDate().toString()
        // Opening the app or rebooting after 9h must not discard today's delayed alarm.
        val next = if (alreadyScheduledToday && NotificationPolicy.canSendDaily(now, lastSent)) {
            now.plusSeconds(5)
        } else {
            NotificationPolicy.nextDailyTime(now)
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), dailyIntent())
        preferences.edit().putString("scheduled_date", next.toLocalDate().toString()).apply()
    }

    fun deliverDaily(now: ZonedDateTime = ZonedDateTime.now()) {
        try {
            val user = currentUser() ?: return
            val lastSent = preferences.getString("daily_last_date", null)?.let(LocalDate::parse)
            if (!NotificationPolicy.canSendDaily(now, lastSent)) return
            val messages = context.resources.getStringArray(R.array.notification_daily_messages)
            val message = messages[NotificationPolicy.messageIndex(now.toLocalDate().toEpochDay(), messages.size)]
            if (post(DAILY_ID, DAILY_CHANNEL, context.getString(R.string.notification_daily_title, NotificationPolicy.firstName(user.name)), message)) {
                // Commit before the receiver exits to prevent duplicate delivery on process restart.
                if (!preferences.edit().putString("daily_last_date", now.toLocalDate().toString()).commit()) {
                    Log.w(TAG, "Could not persist daily notification delivery")
                }
            }
        } finally {
            scheduleDaily(now)
        }
    }

    fun celebrateRecord(existingRecordId: String?, savedRecordId: String) {
        val user = currentUser() ?: return
        if (!NotificationPolicy.shouldCelebrate(existingRecordId, savedRecordId, preferences.getString("last_celebrated_id", null))) return
        val messages = context.resources.getStringArray(R.array.notification_saved_messages)
        val message = messages[NotificationPolicy.messageIndex(savedRecordId.hashCode().toLong(), messages.size)]
        if (post(SAVED_ID, SAVED_CHANNEL, context.getString(R.string.notification_saved_title, NotificationPolicy.firstName(user.name)), message)) {
            preferences.edit().putString("last_celebrated_id", savedRecordId).apply()
        }
    }

    fun cancelForLogout() {
        alarms.cancel(dailyIntent())
        manager.cancel(DAILY_ID)
        manager.cancel(SAVED_ID)
    }

    private fun post(id: Int, channel: String, title: String, message: String): Boolean {
        createChannels()
        if (!canNotify(channel)) return false
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_JOURNAL, true)
        }
        val contentIntent = PendingIntent.getActivity(context, id, openApp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val publicVersion = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification_heart)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_private_preview))
            .build()
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification_heart)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .build()
        return try {
            manager.notify(id, notification)
            true
        } catch (error: SecurityException) {
            Log.w(TAG, "Notification permission was revoked")
            false
        }
    }

    private fun dailyIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, DAILY_ID,
        Intent(context, DailyNotificationReceiver::class.java).setAction(ACTION_DAILY),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val DAILY_CHANNEL = "daily_motivation"
        const val SAVED_CHANNEL = "record_encouragement"
        const val EXTRA_OPEN_JOURNAL = "open_journal_from_notification"
        const val ACTION_DAILY = "br.com.mcoder.primeiroprojeto.DAILY_MOTIVATION"
        private const val DAILY_ID = 2001
        private const val SAVED_ID = 2002
        private const val TAG = "AppNotifications"
    }
}
