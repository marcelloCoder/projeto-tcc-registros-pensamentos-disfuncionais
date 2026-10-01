package br.com.mcoder.primeiroprojeto.model

object ThoughtBelief {
    val range = 0..10

    fun isValid(value: Int?): Boolean = value != null && value in range
}
