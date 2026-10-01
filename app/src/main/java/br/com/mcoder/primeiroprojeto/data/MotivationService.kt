package br.com.mcoder.primeiroprojeto.data

import br.com.mcoder.primeiroprojeto.util.MotivationPhrases
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

object MotivationService {
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.5-flash-lite")
    }

    suspend fun generatePhrases(): List<String> {
        // Only this generic prompt is sent. Diary records and account data stay on device.
        val response = model.generateContent(
            """
            Crie 20 frases diferentes de acolhimento e motivação em português brasileiro
            para um aplicativo de registro de pensamentos. Use um tom calmo, respeitoso
            e sem julgamento, sobre autocompaixão, pausa e pequenos passos.
            Cada frase deve ter no máximo 160 caracteres e ser independente das outras.
            Não ofereça diagnósticos, tratamentos, conselhos médicos ou promessas de cura.
            Responda apenas com uma frase por linha, sem títulos, listas ou numeração.
            """.trimIndent()
        )
        return MotivationPhrases.parse(response.text.orEmpty()).also {
            check(it.isNotEmpty()) { "Empty motivational response" }
        }
    }
}
