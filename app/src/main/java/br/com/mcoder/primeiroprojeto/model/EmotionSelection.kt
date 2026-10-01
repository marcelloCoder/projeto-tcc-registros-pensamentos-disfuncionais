package br.com.mcoder.primeiroprojeto.model

enum class EmotionOption(val label: String) {
    JOY("Alegria"),
    ANXIETY("Ansiedade"),
    SADNESS("Tristeza"),
    DISCOURAGEMENT("Desânimo"),
    ANGER("Raiva"),
    FEAR("Medo"),
    SURPRISE("Surpresa"),
    EMBARRASSMENT("Vergonha"),
    SLEEPINESS("Sono")
}

class EmotionSelection(savedValue: String? = null) {
    var value: String = savedValue.orEmpty()
        private set

    val selectedOption: EmotionOption?
        get() = EmotionOption.entries.firstOrNull { it.label.equals(value.trim(), ignoreCase = true) }

    fun select(option: EmotionOption) {
        value = option.label
    }

    fun isValid(): Boolean = value.isNotBlank()
}
