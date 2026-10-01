package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.ui.BottomNavigationSelectionGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomNavigationSelectionGuardTest {
    @Test
    fun blocksSelectionHandlingDuringProgrammaticUpdate() {
        val guard = BottomNavigationSelectionGuard()

        assertTrue(guard.shouldHandleSelection())

        guard.runProgrammaticUpdate {
            assertFalse(guard.shouldHandleSelection())
        }

        assertTrue(guard.shouldHandleSelection())
    }
}
