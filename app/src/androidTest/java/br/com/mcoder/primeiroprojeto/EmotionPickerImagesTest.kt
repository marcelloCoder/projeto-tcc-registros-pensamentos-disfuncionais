package br.com.mcoder.primeiroprojeto

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.GridLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.ui.EmotionImages
import br.com.mcoder.primeiroprojeto.databinding.ItemEmotionBinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EmotionPickerImagesTest {
    @Test
    fun pickerDisplaysEqualImagesAndKeepsOnlyTheChosenEmotionSelected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.onCreateThoughtRequested()
                activity.supportFragmentManager.executePendingTransactions()
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                val grid = activity.findViewById<GridLayout>(R.id.emotionGrid)
                val cards = (0 until grid.childCount).map { ItemEmotionBinding.bind(grid.getChildAt(it)) }
                assertEquals(EmotionOption.entries.size, cards.size)
                assertEquals(1, cards.map { it.imageEmotion.width to it.imageEmotion.height }.toSet().size)
                cards.forEachIndexed { index, card ->
                    assertEquals(card.imageEmotion.width, card.imageEmotion.height)
                    assertTrue(card.imageEmotion.width > 0)
                    assertEquals(EmotionOption.entries[index].label, card.textEmotionLabel.text.toString())
                    card.root.performClick()
                    cards.forEach { assertEquals(it === card, it.root.isChecked) }
                }
                grid.requestRectangleOnScreen(Rect(0, 0, grid.width, grid.height), true)
            }
            instrumentation.waitForIdleSync()
            val preview = File(instrumentation.targetContext.getExternalFilesDir(null), "emoji-picker-preview.png")
            preview.outputStream().use { output ->
                instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        }
    }

    @Test
    fun allSuppliedImagesFillThePickerWithoutMarginsOrEmbeddedCaptions() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        EmotionOption.entries.forEach { option ->
            val bitmap = EmotionImages.forPicker(resources, option)
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val opaqueFraction = pixels.count { it ushr 24 >= 128 }.toFloat() / pixels.size
            assertTrue("${option.label}: face should fill its image", opaqueFraction > 0.85f)
            val aspectRatio = bitmap.width.toFloat() / bitmap.height
            assertTrue("${option.label}: preserve face proportions", aspectRatio in 0.9f..1.1f)
        }
    }
}
