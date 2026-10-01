package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.util.ThoughtFilterEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ThoughtFilterEngineTest {
    private val records = listOf(
        ThoughtRecord(
            id = "1",
            userId = "user-1",
            situation = "Meeting with manager",
            automaticThinking = "I will fail",
            emotional = "Anxious",
            dateTimeMillis = 2_000L,
            createdAt = 1_000L,
            updatedAt = 1_000L
        ),
        ThoughtRecord(
            id = "2",
            userId = "user-1",
            situation = "Family dinner",
            automaticThinking = "Nobody listens to me",
            emotional = "Sad",
            dateTimeMillis = 3_000L,
            createdAt = 1_000L,
            updatedAt = 1_000L
        )
    )

    @Test
    fun apply_filtersBySearchAndSortsNewestFirst() {
        val result = ThoughtFilterEngine.apply(
            records,
            ThoughtQuery(searchTerm = "manager")
        )

        assertEquals(listOf("1"), result.map { it.id })
    }

    @Test
    fun allEmotionsOption_inPortugueseReturnsEveryRecord() {
        val option = ThoughtFilterEngine.emotionOptions(records).first()

        assertEquals("Todas as emoções", option)
        assertEquals(listOf("2", "1"), ThoughtFilterEngine.apply(
            records, ThoughtQuery(emotion = option)
        ).map { it.id })
    }

    @Test
    fun apply_findsTheSensationWithoutMixingItIntoTheEventAnswer() {
        val withSensation = records[0].copy(distressingSensation = "Aperto no peito")
        val result = ThoughtFilterEngine.apply(
            listOf(withSensation, records[1]), ThoughtQuery(searchTerm = "APERTO")
        )
        assertEquals(listOf(withSensation), result)
        assertEquals("Meeting with manager", result.single().situation)
        assertEquals("Aperto no peito", result.single().distressingSensation)
    }

    @Test
    fun legacyRecordWithNoSensation_stillSupportsSituationSearch() {
        assertEquals("", records[0].distressingSensation)
        assertEquals(listOf(records[0]), ThoughtFilterEngine.apply(
            records, ThoughtQuery(searchTerm = "manager")
        ))
    }

    @Test
    fun apply_filtersByEmotionIgnoringCase() {
        val result = ThoughtFilterEngine.apply(
            records,
            ThoughtQuery(emotion = "sad")
        )

        assertEquals(listOf("2"), result.map { it.id })
    }
}
