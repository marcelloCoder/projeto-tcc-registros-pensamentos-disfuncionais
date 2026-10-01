package br.com.mcoder.primeiroprojeto.data

import br.com.mcoder.primeiroprojeto.model.ThoughtDraft
import br.com.mcoder.primeiroprojeto.model.EmotionIntensity
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.model.ThoughtBelief
import br.com.mcoder.primeiroprojeto.util.IdGenerator

class ThoughtRepository(
    private val storage: AppStorage,
    private val authRepository: AuthRepository
) {
    fun getThoughtsForCurrentUser(): List<ThoughtRecord> {
        val currentUser = authRepository.getCurrentUser() ?: return emptyList()
        return storage.loadThoughts()
            .filter { it.userId == currentUser.id }
            .sortedByDescending { it.dateTimeMillis }
    }

    fun getThought(recordId: String): ThoughtRecord? {
        val currentUser = authRepository.getCurrentUser() ?: return null
        return storage.loadThoughts()
            .firstOrNull { it.id == recordId && it.userId == currentUser.id }
    }

    fun saveThought(draft: ThoughtDraft): Result<ThoughtRecord> {
        val currentUser = authRepository.getCurrentUser() ?: return failure("Entre na sua conta novamente.")
        val situation = draft.situation.trim()
        val distressingSensation = draft.distressingSensation.trim()
        val automaticThinking = draft.automaticThinking.trim()
        val emotional = draft.emotional.trim()

        if (situation.isBlank()) {
            return failure("Informe a situação.")
        }
        if (automaticThinking.isBlank()) {
            return failure("Informe o pensamento automático.")
        }
        if (!ThoughtBelief.isValid(draft.thoughtBelief)) {
            return failure("Selecione quanto você acreditou no pensamento, de 0 a 10.")
        }
        if (emotional.isBlank()) {
            return failure("Selecione uma emoção.")
        }
        if (!EmotionIntensity.isValid(draft.emotionIntensity)) {
            return failure("Selecione a intensidade da emoção de 0 a 10.")
        }

        val thoughts = storage.loadThoughts().toMutableList()
        val now = System.currentTimeMillis()
        val existingIndex = draft.id?.let { recordId ->
            thoughts.indexOfFirst { it.id == recordId && it.userId == currentUser.id }
        } ?: -1

        val record = if (existingIndex >= 0) {
            val existing = thoughts[existingIndex]
            existing.copy(
                situation = situation,
                distressingSensation = distressingSensation,
                automaticThinking = automaticThinking,
                thoughtBelief = draft.thoughtBelief,
                emotional = emotional,
                emotionIntensity = draft.emotionIntensity,
                dateTimeMillis = draft.dateTimeMillis,
                updatedAt = now
            )
        } else {
            ThoughtRecord(
                id = IdGenerator.next("thought"),
                userId = currentUser.id,
                situation = situation,
                distressingSensation = distressingSensation,
                automaticThinking = automaticThinking,
                thoughtBelief = draft.thoughtBelief,
                emotional = emotional,
                emotionIntensity = draft.emotionIntensity,
                dateTimeMillis = draft.dateTimeMillis,
                createdAt = now,
                updatedAt = now
            )
        }

        if (existingIndex >= 0) {
            thoughts[existingIndex] = record
        } else {
            thoughts += record
        }

        storage.saveThoughts(thoughts)
        return Result.success(record)
    }

    fun deleteThought(recordId: String): Result<Unit> {
        val currentUser = authRepository.getCurrentUser() ?: return failure("Entre na sua conta novamente.")
        val thoughts = storage.loadThoughts().toMutableList()
        val removed = thoughts.removeAll { it.id == recordId && it.userId == currentUser.id }

        if (!removed) {
            return failure("Este registro não foi encontrado.")
        }

        storage.saveThoughts(thoughts)
        return Result.success(Unit)
    }

    private fun <T> failure(message: String): Result<T> {
        return Result.failure(IllegalArgumentException(message))
    }
}
