package br.com.mcoder.primeiroprojeto.util

import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import java.util.Locale
import java.time.Instant
import java.time.ZoneId

object ThoughtFilterEngine {
    fun apply(
        records: List<ThoughtRecord>,
        query: ThoughtQuery,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<ThoughtRecord> {
        val searchTerm = query.searchTerm.trim().lowercase(Locale.getDefault())
        val emotionFilter = query.emotion.trim()

        return records
            .asSequence()
            .filter { record ->
                val date = Instant.ofEpochMilli(record.dateTimeMillis).atZone(zone).toLocalDate()
                (query.dateFrom == null || !date.isBefore(query.dateFrom)) &&
                    (query.dateTo == null || !date.isAfter(query.dateTo))
            }
            .filter { record ->
                emotionFilter == ThoughtQuery.ALL_EMOTIONS ||
                    record.emotional.equals(emotionFilter, ignoreCase = true)
            }
            .filter { record ->
                if (searchTerm.isBlank()) {
                    true
                } else {
                    record.situation.contains(searchTerm, ignoreCase = true) ||
                        record.distressingSensation.contains(searchTerm, ignoreCase = true) ||
                        record.automaticThinking.contains(searchTerm, ignoreCase = true) ||
                        record.emotional.contains(searchTerm, ignoreCase = true)
                }
            }
            .sortedByDescending { it.dateTimeMillis }
            .toList()
    }

    fun emotionOptions(records: List<ThoughtRecord>): List<String> {
        val options = records
            .map { it.emotional.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sortedBy { it.lowercase(Locale.getDefault()) }

        return listOf(ThoughtQuery.ALL_EMOTIONS) + options
    }
}
