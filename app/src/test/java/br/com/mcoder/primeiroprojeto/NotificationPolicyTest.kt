package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.notifications.NotificationPolicy
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

class NotificationPolicyTest {
    @Test fun schedulesTodayBeforeNineAndTomorrowAfterNine() {
        assertEquals(ZonedDateTime.parse("2026-10-01T09:00:00-03:00[America/Sao_Paulo]"),
            NotificationPolicy.nextDailyTime(ZonedDateTime.parse("2026-10-01T08:00:00-03:00[America/Sao_Paulo]")))
        assertEquals(ZonedDateTime.parse("2026-10-02T09:00:00-03:00[America/Sao_Paulo]"),
            NotificationPolicy.nextDailyTime(ZonedDateTime.parse("2026-10-01T09:00:00-03:00[America/Sao_Paulo]")))
    }

    @Test fun keepsNineOClockAcrossDaylightSavingChange() {
        val now = ZonedDateTime.parse("2026-03-07T12:00:00-05:00[America/New_York]")
        assertEquals(ZonedDateTime.parse("2026-03-08T09:00:00-04:00[America/New_York]"), NotificationPolicy.nextDailyTime(now))
    }

    @Test fun permitsOnlyOneDailyNotificationPerLocalDate() {
        val now = ZonedDateTime.parse("2026-10-01T09:30:00-03:00[America/Sao_Paulo]")
        assertTrue(NotificationPolicy.canSendDaily(now, null))
        assertTrue(NotificationPolicy.canSendDaily(now, LocalDate.of(2026, 9, 30)))
        assertFalse(NotificationPolicy.canSendDaily(now, now.toLocalDate()))
        assertFalse(NotificationPolicy.canSendDaily(now.minusHours(1), null))
    }

    @Test fun movingClockBackDoesNotRepeatDailyNotification() {
        val now = ZonedDateTime.parse("2026-10-01T10:00:00-03:00[America/Sao_Paulo]")
        assertFalse(NotificationPolicy.canSendDaily(now, LocalDate.of(2026, 10, 2)))
    }

    @Test fun congratulatesCreationButNotEditOrDuplicateSaveCallback() {
        assertTrue(NotificationPolicy.shouldCelebrate(null, "new", "old"))
        assertFalse(NotificationPolicy.shouldCelebrate("existing", "existing", null))
        assertFalse(NotificationPolicy.shouldCelebrate(null, "new", "new"))
    }

    @Test fun personalizesUsingOnlyFirstNameAndHandlesExtraSpaces() {
        assertEquals("Ana", NotificationPolicy.firstName("  Ana   Maria  "))
        assertEquals("João", NotificationPolicy.firstName("João\nSilva"))
    }

    @Test fun variesMessageAcrossDaysAndHandlesNegativeRecordHashes() {
        assertNotEquals(NotificationPolicy.messageIndex(10, 6), NotificationPolicy.messageIndex(11, 6))
        assertTrue(NotificationPolicy.messageIndex(Int.MIN_VALUE.toLong(), 6) in 0..5)
    }
}
