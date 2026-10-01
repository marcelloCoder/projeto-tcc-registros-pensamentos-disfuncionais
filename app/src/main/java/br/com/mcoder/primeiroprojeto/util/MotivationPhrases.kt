package br.com.mcoder.primeiroprojeto.util

/** Keeps rotation and response validation independent from Android and network access. */
class MotivationPhrases {
    private val pending = ArrayDeque<String>()
    var current: String? = null
        private set
    private var nextUpdateAt = 0L

    val needsRefill: Boolean get() = pending.isEmpty()

    fun waitMillis(now: Long): Long = (nextUpdateAt - now).coerceAtLeast(0L)

    fun refill(phrases: List<String>) {
        val valid = phrases.filter { it.isNotBlank() && it.length <= MAX_LENGTH && it != current }
            .distinct().take(BATCH_SIZE)
        require(valid.isNotEmpty()) { "No usable motivational phrases" }
        pending.addAll(valid)
    }

    fun advance(now: Long): String {
        check(waitMillis(now) == 0L) { "Phrase is not due yet" }
        val phrase = pending.removeFirst()
        current = phrase
        nextUpdateAt = now + INTERVAL_MILLIS
        return phrase
    }

    fun retryLater(now: Long) {
        nextUpdateAt = now + INTERVAL_MILLIS
    }

    companion object {
        const val INTERVAL_MILLIS = 60_000L
        const val MAX_LENGTH = 160
        const val BATCH_SIZE = 20
        private val listPrefix = Regex("^\\s*(?:[-*•]|\\d+[.)])\\s*")

        fun parse(response: String): List<String> = response.lineSequence()
            .map { it.replace(listPrefix, "").trim().trim('"', '“', '”') }
            .filter { it.isNotBlank() && it.length <= MAX_LENGTH }
            .distinct().take(BATCH_SIZE).toList()
    }
}
