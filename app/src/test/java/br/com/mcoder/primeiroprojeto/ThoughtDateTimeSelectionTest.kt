package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.ThoughtDateTimeSelection
import org.junit.Assert.assertEquals
import org.junit.Test

class ThoughtDateTimeSelectionTest {
    @Test
    fun newRecordUsesCurrentTimeWhileTypingAndAtSave() {
        var now = 1_000L
        val selection = ThoughtDateTimeSelection(now = { now })
        assertEquals(1_000L, selection.currentTimeMillis())
        now = 61_000L
        assertEquals(61_000L, selection.currentTimeMillis())
    }

    @Test
    fun existingRecordKeepsItsOriginalTimestamp() {
        val selection = ThoughtDateTimeSelection(1_000L, now = { 61_000L })
        assertEquals(1_000L, selection.currentTimeMillis())
    }

    @Test
    fun manualSelectionIsPreservedAfterRestoringTheEditor() {
        val selection = ThoughtDateTimeSelection(now = { 61_000L })
        selection.select(20_000L)
        val restored = ThoughtDateTimeSelection(selection.fixedTimeMillis, now = { 90_000L })
        assertEquals(20_000L, restored.currentTimeMillis())
    }

    @Test
    fun automaticTimeResumesWithCurrentClockAfterRestoringTheEditor() {
        val selection = ThoughtDateTimeSelection(now = { 1_000L })
        val restored = ThoughtDateTimeSelection(selection.fixedTimeMillis, now = { 90_000L })
        assertEquals(90_000L, restored.currentTimeMillis())
    }
}
