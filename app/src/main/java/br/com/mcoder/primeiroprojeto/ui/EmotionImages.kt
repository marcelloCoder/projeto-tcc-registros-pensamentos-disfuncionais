package br.com.mcoder.primeiroprojeto.ui

import androidx.annotation.DrawableRes
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.model.EmotionSelection

object EmotionImages {
    @DrawableRes
    fun forLabel(label: String): Int? = EmotionSelection(label).selectedOption?.let(::forOption)

    @DrawableRes
    fun forOption(option: EmotionOption): Int = when (option) {
        EmotionOption.JOY -> R.drawable.emoji_alegria
        EmotionOption.ANXIETY -> R.drawable.emoji_ansiedade
        EmotionOption.SADNESS -> R.drawable.emoji_tristeza
        EmotionOption.DISCOURAGEMENT -> R.drawable.emoji_desanimo
        EmotionOption.ANGER -> R.drawable.emoji_raiva
        EmotionOption.FEAR -> R.drawable.emoji_medo
        EmotionOption.SURPRISE -> R.drawable.emoji_surpresa
        EmotionOption.EMBARRASSMENT -> R.drawable.emoji_vergonha
        EmotionOption.SLEEPINESS -> R.drawable.emoji_sono
    }
}
