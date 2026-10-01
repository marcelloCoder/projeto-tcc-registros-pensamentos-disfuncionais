package br.com.mcoder.primeiroprojeto.notifications

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object NotificationPolicy {
    val dailyTime: LocalTime = LocalTime.of(9, 0)

    fun nextDailyTime(now: ZonedDateTime): ZonedDateTime {
        val today = now.toLocalDate().atTime(dailyTime).atZone(now.zone)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    fun canSendDaily(now: ZonedDateTime, lastSentDate: LocalDate?): Boolean =
        !now.toLocalTime().isBefore(dailyTime) &&
            (lastSentDate == null || now.toLocalDate().isAfter(lastSentDate))

    fun shouldCelebrate(existingRecordId: String?, savedRecordId: String, lastCelebratedId: String?): Boolean =
        existingRecordId == null && savedRecordId != lastCelebratedId

    fun firstName(name: String): String = name.trim().split(Regex("\\s+")).first().take(40)

    fun messageIndex(seed: Long, count: Int): Int = Math.floorMod(seed, count.toLong()).toInt()
}
