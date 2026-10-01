package br.com.mcoder.primeiroprojeto.util

import java.security.MessageDigest

object PasswordHasher {
    fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
