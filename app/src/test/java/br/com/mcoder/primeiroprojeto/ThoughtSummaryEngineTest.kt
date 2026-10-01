package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.util.ThoughtFilterEngine
import br.com.mcoder.primeiroprojeto.util.ThoughtSummaryEngine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThoughtSummaryEngineTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private fun date(value: String) = LocalDate.parse(value)
    private fun record(id: String, instant: String, emotion: String = "Medo") = ThoughtRecord(
        id, "user", "Trabalho", "Pensamento", emotion, Instant.parse(instant).toEpochMilli(), 0L, 0L
    )

    @Test
    fun periodIncludesEntireLocalEndDayAndExcludesAdjacentDays() {
        val records = listOf(
            record("before", "2026-09-24T02:59:59Z"),
            record("start", "2026-09-24T03:00:00Z"),
            record("end", "2026-09-25T02:59:59.999Z"),
            record("after", "2026-09-25T03:00:00Z")
        )
        val query = ThoughtQuery(dateFrom = date("2026-09-24"), dateTo = date("2026-09-24"))
        assertEquals(listOf("end", "start"), ThoughtFilterEngine.apply(records, query, zone).map { it.id })
    }

    @Test
    fun periodCombinesWithTextAndEmotionFilters() {
        val records = listOf(
            record("match", "2026-09-24T12:00:00Z"),
            record("otherEmotion", "2026-09-24T12:00:00Z", "Alegria"),
            record("outside", "2026-09-23T12:00:00Z")
        )
        val query = ThoughtQuery("trabalho", "medo", date("2026-09-24"), date("2026-09-24"))
        assertEquals(listOf("match"), ThoughtFilterEngine.apply(records, query, zone).map { it.id })
    }

    @Test
    fun mostFrequentNormalizesLabelsAndReportsEveryTie() {
        val records = listOf(" medo ", "MEDO", "alegria", "Alegria", "Tristeza").mapIndexed { index, emotion ->
            record(index.toString(), "2026-09-24T12:00:00Z", emotion)
        }
        val summary = ThoughtSummaryEngine.summarize(records, date("2026-09-01"), date("2026-09-30"), zone)
        assertEquals(5, summary.total)
        assertEquals(listOf("Alegria", "Medo"), summary.mostFrequentEmotions)
        assertEquals(2, summary.emotionCount)
    }

    @Test
    fun weeklyCountsIncludeZeroWeeksAndClipPartialWeeksAcrossNewYear() {
        val records = listOf(
            record("sunday", "2025-12-28T15:00:00Z"),
            record("monday", "2025-12-29T15:00:00Z")
        )
        val summary = ThoughtSummaryEngine.summarize(records, date("2025-12-28"), date("2026-01-06"), zone)
        assertEquals(listOf(0, 1, 1), summary.weeks.map { it.count })
        assertEquals(date("2026-01-05"), summary.weeks.first().from)
        assertEquals(date("2026-01-06"), summary.weeks.first().to)
        assertEquals(date("2025-12-28"), summary.weeks.last().from)
        assertEquals(summary.total, summary.weeks.sumOf { it.count })
    }

    @Test
    fun emptyPeriodDoesNotInventAnEmotion() {
        val summary = ThoughtSummaryEngine.summarize(emptyList(), date("2026-09-24"), date("2026-09-24"), zone)
        assertEquals(0, summary.total)
        assertEquals(0, summary.emotionCount)
        assertTrue(summary.mostFrequentEmotions.isEmpty())
        assertEquals(listOf(0), summary.weeks.map { it.count })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReversedPeriod() {
        ThoughtSummaryEngine.summarize(emptyList(), date("2026-09-25"), date("2026-09-24"), zone)
    }
}
