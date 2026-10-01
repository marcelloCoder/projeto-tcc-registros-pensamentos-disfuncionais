package br.com.mcoder.primeiroprojeto.model

data class ThoughtDraft(
    val id: String? = null,
    val situation: String,
    val automaticThinking: String,
    val emotional: String,
    val dateTimeMillis: Long,
    val emotionIntensity: Int? = null,
    val distressingSensation: String = "",
    val thoughtBelief: Int? = null
)
