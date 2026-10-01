package br.com.mcoder.primeiroprojeto

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.data.MotivationService
import br.com.mcoder.primeiroprojeto.util.MotivationPhrases
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Explicit opt-in: ordinary tests do not make network calls or consume Gemini quota. */
@RunWith(AndroidJUnit4::class)
class MotivationIntegrationTest {
    @Test
    fun generatesUsablePhrasesWithRegisteredAppCheckToken() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyAi") == "true")
        AppGraph.initialize(InstrumentationRegistry.getInstrumentation().targetContext)
        val phrases = try {
            withTimeout(30_000L) { MotivationService.generatePhrases() }
        } catch (error: Exception) {
            // Keep cloud diagnostics useful without exposing URLs, credentials or project IDs.
            val safeMessage = error.message.orEmpty()
                .replace(Regex("https?://\\S+"), "[URL omitida]")
                .replace(Regex("AIza[\\w-]+"), "[chave omitida]")
                .replace(Regex("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}"), "[token omitido]")
                .replace(Regex("\\b\\d{6,}\\b"), "[identificador omitido]")
            throw AssertionError("${error.javaClass.simpleName}: ${safeMessage.take(1500)}")
        }
        assertTrue(phrases.isNotEmpty())
        assertTrue(phrases.all { it.isNotBlank() && it.length <= MotivationPhrases.MAX_LENGTH })
        assertTrue(phrases.size <= MotivationPhrases.BATCH_SIZE)
    }
}
