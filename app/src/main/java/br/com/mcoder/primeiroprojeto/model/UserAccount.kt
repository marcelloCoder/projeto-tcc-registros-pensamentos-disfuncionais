package br.com.mcoder.primeiroprojeto.model

data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long
)
