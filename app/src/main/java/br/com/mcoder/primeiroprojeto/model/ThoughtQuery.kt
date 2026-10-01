package br.com.mcoder.primeiroprojeto.model

import java.time.LocalDate

data class ThoughtQuery(
    val searchTerm: String = "",
    val emotion: String = ALL_EMOTIONS,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null
) {
    companion object {
        const val ALL_EMOTIONS = "Todas as emoções"
    }
}
