package br.com.mcoder.primeiroprojeto.util

import java.util.UUID

object IdGenerator {
    fun next(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}
