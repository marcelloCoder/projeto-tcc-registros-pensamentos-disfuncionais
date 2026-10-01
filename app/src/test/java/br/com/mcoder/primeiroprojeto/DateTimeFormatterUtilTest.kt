package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.util.DateTimeFormatterUtil
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeFormatterUtilTest {
    @Test
    fun formatsBrazilianDateAnd24HourTime_evenWithEnglishDeviceLocale() {
        val previousLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val timestamp = Calendar.getInstance().apply {
                clear()
                set(2026, Calendar.SEPTEMBER, 23, 17, 5, 42)
            }.timeInMillis

            assertEquals("23/09/2026", DateTimeFormatterUtil.formatDate(timestamp))
            assertEquals("17:05", DateTimeFormatterUtil.formatTime(timestamp))
            assertEquals("23/09/2026, 17:05", DateTimeFormatterUtil.formatFullDateTime(timestamp))
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Test
    fun usesDeviceTimeZoneIncludingChangesWhileAppIsOpen() {
        val previousTimeZone = TimeZone.getDefault()
        val timestamp = Instant.parse("2026-09-24T01:05:42Z").toEpochMilli()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"))
            assertEquals("23/09/2026, 22:05", DateTimeFormatterUtil.formatFullDateTime(timestamp))
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            assertEquals("24/09/2026, 01:05", DateTimeFormatterUtil.formatFullDateTime(timestamp))
        } finally {
            TimeZone.setDefault(previousTimeZone)
        }
    }
}
