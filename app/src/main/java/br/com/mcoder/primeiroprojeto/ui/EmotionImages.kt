package br.com.mcoder.primeiroprojeto.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.model.EmotionSelection

object EmotionImages {
    fun forPicker(resources: Resources, option: EmotionOption): Bitmap {
        val bitmap = BitmapFactory.decodeResource(
            resources, forOption(option), BitmapFactory.Options().apply { inSampleSize = 4 }
        )
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Each supplied PNG has a face, a transparent gap and an embedded caption.
        // Ignore sparse edge artifacts and use only the first substantial region.
        val minimumRowPixels = (bitmap.width / 20).coerceAtLeast(1)
        val rows = (0 until bitmap.height).map { y ->
            (0 until bitmap.width).count { x -> pixels[y * bitmap.width + x] ushr 24 >= 128 }
        }
        val top = rows.indexOfFirst { it >= minimumRowPixels }
        if (top < 0) return bitmap
        val bottom = (top until bitmap.height).firstOrNull { rows[it] < minimumRowPixels }
            ?: bitmap.height
        var left = bitmap.width
        var right = 0
        for (y in top until bottom) {
            for (x in 0 until bitmap.width) {
                if (pixels[y * bitmap.width + x] ushr 24 >= 128) {
                    left = minOf(left, x)
                    right = maxOf(right, x + 1)
                }
            }
        }
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

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
