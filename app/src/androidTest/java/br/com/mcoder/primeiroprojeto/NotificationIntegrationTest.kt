package br.com.mcoder.primeiroprojeto

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.mcoder.primeiroprojeto.model.UserAccount
import br.com.mcoder.primeiroprojeto.notifications.AppNotifications
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

/** Opt-in integration check: posts test notifications without creating diary records. */
@RunWith(AndroidJUnit4::class)
class NotificationIntegrationTest {
    @Test fun deliversDailyAndCongratulationsAndCancelsOnLogout() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyNotifications") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").use {
                android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
            }
        }
        val preferences = context.getSharedPreferences("notification_integration_test", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        var loggedIn = true
        val notifications = AppNotifications(context, {
            if (loggedIn) UserAccount("notification-test", "Ana Teste", "", "", 0L) else null
        }, "notification_integration_test")
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            notifications.createChannels()
            assertTrue(notifications.canNotify(AppNotifications.DAILY_CHANNEL))
            notifications.deliverDaily(ZonedDateTime.now().withHour(9).withMinute(5))
            val daily = manager.activeNotifications.single { it.notification.channelId == AppNotifications.DAILY_CHANNEL }
            assertTrue(daily.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty().contains("Ana"))
            assertTrue(daily.notification.extras.getString(Notification.EXTRA_TEXT).orEmpty().isNotBlank())
            assertEquals(Notification.VISIBILITY_PRIVATE, daily.notification.visibility)
            assertNotNull(daily.notification.contentIntent)

            notifications.celebrateRecord(null, "notification-test-record")
            val saved = manager.activeNotifications.single { it.notification.channelId == AppNotifications.SAVED_CHANNEL }
            assertTrue(saved.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty().contains("Parabéns"))
            manager.cancel(saved.id)
            notifications.celebrateRecord("notification-test-record", "notification-test-record")
            assertFalse(manager.activeNotifications.any { it.notification.channelId == AppNotifications.SAVED_CHANNEL })

            loggedIn = false
            notifications.cancelForLogout()
            notifications.celebrateRecord(null, "another-test-record")
            assertFalse(manager.activeNotifications.any { it.notification.channelId in listOf(AppNotifications.DAILY_CHANNEL, AppNotifications.SAVED_CHANNEL) })
        } finally {
            notifications.cancelForLogout()
            preferences.edit().clear().commit()
            AppNotifications(context).scheduleDaily()
        }
    }
}
