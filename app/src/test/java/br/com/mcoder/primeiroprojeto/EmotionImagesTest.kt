package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.ui.EmotionImages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmotionImagesTest {
    @Test
    fun savedCaptions_showTheirRespectiveImages() {
        val expected = mapOf(
            "Alegria" to R.drawable.emoji_alegria,
            "Ansiedade" to R.drawable.emoji_ansiedade,
            "Tristeza" to R.drawable.emoji_tristeza,
            "Desânimo" to R.drawable.emoji_desanimo,
            "Raiva" to R.drawable.emoji_raiva,
            "Medo" to R.drawable.emoji_medo,
            "Surpresa" to R.drawable.emoji_surpresa,
            "Vergonha" to R.drawable.emoji_vergonha,
            "Sono" to R.drawable.emoji_sono
        )
        expected.forEach { (caption, image) ->
            assertEquals(image, EmotionImages.forLabel(caption))
        }
    }

    @Test
    fun savedCaption_matchesIgnoringCaseAndSurroundingSpaces() {
        assertEquals(R.drawable.emoji_desanimo, EmotionImages.forLabel("  DESÂNIMO  "))
    }

    @Test
    fun legacyTextAndMissingEmotion_doNotReceiveAnUnrelatedEmoji() {
        assertNull(EmotionImages.forLabel("Frustrado e cansado"))
        assertNull(EmotionImages.forLabel(""))
    }
}
