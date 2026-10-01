package br.com.mcoder.primeiroprojeto.model

class ThoughtDateTimeSelection(
    fixedTimeMillis: Long? = null,
    private val now: () -> Long = System::currentTimeMillis
) {
    var fixedTimeMillis: Long? = fixedTimeMillis
        private set

    fun currentTimeMillis(): Long = fixedTimeMillis ?: now()

    fun select(timestamp: Long) {
        fixedTimeMillis = timestamp
    }
}
