package br.com.mcoder.primeiroprojeto.model

data class ThoughtRecord(
    val id: String,
    val userId: String,
    val situation: String,
    val automaticThinking: String,
    val emotional: String,
    val dateTimeMillis: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val emotionIntensity: Int? = null,
    val distressingSensation: String = "",
    val thoughtBelief: Int? = null
)
