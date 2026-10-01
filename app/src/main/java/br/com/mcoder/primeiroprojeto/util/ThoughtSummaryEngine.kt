package br.com.mcoder.primeiroprojeto.util

import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeeklyThoughtCount(val from: LocalDate, val to: LocalDate, val count: Int)

data class ThoughtSummary(
    val total: Int,
    val mostFrequentEmotions: List<String>,
    val emotionCount: Int,
    val weeks: List<WeeklyThoughtCount>
)

object ThoughtSummaryEngine {
    fun summarize(
        records: List<ThoughtRecord>,
        from: LocalDate,
        to: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): ThoughtSummary {
        require(!from.isAfter(to)) { "A data inicial deve ser anterior ou igual à data final." }
        val filtered = ThoughtFilterEngine.apply(records, ThoughtQuery(dateFrom = from, dateTo = to), zone)
        val emotionGroups = filtered.map { it.emotional.trim() }.filter { it.isNotEmpty() }
            .groupBy { it.lowercase(Locale.ROOT) }
        val highestCount = emotionGroups.values.maxOfOrNull { it.size } ?: 0
        val mostFrequent = emotionGroups.filterValues { it.size == highestCount }.map { (key, values) ->
            EmotionOption.entries.firstOrNull { it.label.lowercase(Locale.ROOT) == key }?.label
                ?: values.first()
        }.sorted()

        fun weekStart(date: LocalDate) = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val counts = filtered.groupingBy {
            weekStart(Instant.ofEpochMilli(it.dateTimeMillis).atZone(zone).toLocalDate())
        }.eachCount()
        val weeks = generateSequence(weekStart(from)) { it.plusWeeks(1) }
            .takeWhile { !it.isAfter(to) }
            .map { start ->
                WeeklyThoughtCount(maxOf(start, from), minOf(start.plusDays(6), to), counts[start] ?: 0)
            }.toList().reversed()
        return ThoughtSummary(filtered.size, mostFrequent, highestCount, weeks)
    }
}
