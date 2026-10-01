package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.ThoughtBelief
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThoughtBeliefTest {
    @Test
    fun acceptsAllElevenChoicesIncludingZeroAndTen() {
        (0..10).forEach { assertTrue(ThoughtBelief.isValid(it)) }
    }

    @Test
    fun requiresAnExplicitChoiceAndRejectsValuesOutsideTheScale() {
        assertFalse(ThoughtBelief.isValid(null))
        listOf(-1, 11, Int.MIN_VALUE, Int.MAX_VALUE).forEach {
            assertFalse(ThoughtBelief.isValid(it))
        }
    }

    @Test
    fun legacyRecordHasNoInventedBeliefRating() {
        val record = ThoughtRecord("1", "user", "Situação", "Pensamento", "Medo", 1L, 1L, 1L)
        assertNull(record.thoughtBelief)
    }

    @Test
    fun changingBeliefDoesNotChangeTheThoughtOrEmotionIntensity() {
        val record = ThoughtRecord(
            "1", "user", "Situação", "Vou falhar", "Medo", 1L, 1L, 1L,
            emotionIntensity = 8, thoughtBelief = 10
        )
        val edited = record.copy(thoughtBelief = 0)
        assertEquals(0, edited.thoughtBelief)
        assertEquals(8, edited.emotionIntensity)
        assertEquals("Vou falhar", edited.automaticThinking)
    }
}
