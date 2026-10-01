package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.EmotionIntensity
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmotionIntensityTest {
    @Test
    fun acceptsEveryWholeNumberFromZeroToTen() {
        (0..10).forEach { assertTrue(EmotionIntensity.isValid(it)) }
    }

    @Test
    fun missingValueIsDifferentFromExplicitZero() {
        assertFalse(EmotionIntensity.isValid(null))
        assertTrue(EmotionIntensity.isValid(0))
    }

    @Test
    fun rejectsValuesOutsideTheScale() {
        listOf(-1, 11, Int.MIN_VALUE, Int.MAX_VALUE).forEach {
            assertFalse(EmotionIntensity.isValid(it))
        }
    }

    @Test
    fun legacyRecordRemainsUnratedUntilAnIntensityIsChosen() {
        val legacy = ThoughtRecord("1", "user", "Situação", "Pensamento", "Medo", 1L, 1L, 1L)
        assertNull(legacy.emotionIntensity)
        val edited = legacy.copy(emotionIntensity = 7)
        assertEquals(7, edited.emotionIntensity)
        assertEquals(legacy.emotional, edited.emotional)
    }
}
