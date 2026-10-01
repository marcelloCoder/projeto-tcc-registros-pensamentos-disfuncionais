package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.util.MotivationPhrases
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationPhrasesTest {
    @Test
    fun cleansNumberingAndQuotesAndRejectsEmptyLongAndDuplicatePhrases() {
        val response = "1. Respire com calma.\n\n- “Um passo de cada vez.”\nRespire com calma.\n" +
            "x".repeat(161)
        assertEquals(listOf("Respire com calma.", "Um passo de cada vez."), MotivationPhrases.parse(response))
    }

    @Test
    fun limitsBatchSize() {
        assertEquals(20, MotivationPhrases.parse((1..30).joinToString("\n") { "Frase $it" }).size)
    }

    @Test
    fun rotatesOnlyAfterOneMinuteAndUsesCachedPhrases() {
        val phrases = MotivationPhrases()
        phrases.refill(listOf("Primeira frase", "Segunda frase"))
        assertEquals("Primeira frase", phrases.advance(1_000L))
        assertEquals(1L, phrases.waitMillis(60_999L))
        assertEquals(0L, phrases.waitMillis(61_000L))
        assertEquals("Segunda frase", phrases.advance(61_000L))
        assertTrue(phrases.needsRefill)
    }

    @Test(expected = IllegalStateException::class)
    fun cannotRotateBeforeOneMinute() {
        val phrases = MotivationPhrases()
        phrases.refill(listOf("Primeira", "Segunda"))
        phrases.advance(0L)
        phrases.advance(59_999L)
    }

    @Test
    fun failurePreservesCurrentPhraseAndDelaysRetry() {
        val phrases = MotivationPhrases()
        phrases.refill(listOf("Acolha seus pensamentos."))
        phrases.advance(0L)
        phrases.retryLater(60_000L)
        assertEquals("Acolha seus pensamentos.", phrases.current)
        assertEquals(60_000L, phrases.waitMillis(60_000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBatchThatOnlyRepeatsCurrentPhrase() {
        val phrases = MotivationPhrases()
        phrases.refill(listOf("Frase atual"))
        phrases.advance(0L)
        phrases.refill(listOf("Frase atual", ""))
    }
}
