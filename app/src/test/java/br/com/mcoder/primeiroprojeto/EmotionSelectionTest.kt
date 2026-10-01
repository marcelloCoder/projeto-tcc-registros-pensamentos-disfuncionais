package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.model.EmotionSelection
import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.util.ThoughtFilterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmotionSelectionTest {
    @Test
    fun newEntry_requiresAnEmojiAndSavesOnlyTheLatestChoice() {
        val selection = EmotionSelection()
        assertFalse(selection.isValid())

        selection.select(EmotionOption.JOY)
        selection.select(EmotionOption.ANXIETY)

        assertTrue(selection.isValid())
        assertEquals("Ansiedade", selection.value)
        assertEquals(EmotionOption.ANXIETY, selection.selectedOption)
    }

    @Test
    fun everyEmoji_restoresFromItsSavedCaption() {
        EmotionOption.entries.forEach { option ->
            val selection = EmotionSelection()
            selection.select(option)
            assertEquals(option, EmotionSelection(selection.value).selectedOption)
        }
    }

    @Test
    fun editingLegacyEntry_preservesTextUntilAnEmojiIsChosen() {
        val selection = EmotionSelection("Frustrated and tired")
        assertTrue(selection.isValid())
        assertNull(selection.selectedOption)
        assertEquals("Frustrated and tired", selection.value)

        selection.select(EmotionOption.DISCOURAGEMENT)
        assertEquals("Desânimo", selection.value)
    }

    @Test
    fun emojiCaption_remainsSearchableAndFilterableInJournal() {
        val selection = EmotionSelection().apply { select(EmotionOption.SADNESS) }
        val record = ThoughtRecord("1", "user", "Situation", "Thought", selection.value, 1L, 1L, 1L)

        assertEquals(listOf(record), ThoughtFilterEngine.apply(
            listOf(record), ThoughtQuery(searchTerm = "tristeza", emotion = "Tristeza")
        ))
    }
}
