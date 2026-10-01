package br.com.mcoder.primeiroprojeto.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.databinding.ItemThoughtBinding
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.util.DateTimeFormatterUtil

class ThoughtListAdapter(
    context: Context,
    private val onMenuClick: (View, ThoughtRecord) -> Unit
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val items = mutableListOf<ThoughtRecord>()
    private val emotionBitmaps = mutableMapOf<Int, Bitmap>()

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): ThoughtRecord = items[position]

    override fun getItemId(position: Int): Long = items[position].id.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemThoughtBinding.inflate(inflater, parent, false).also { inflated ->
                inflated.root.tag = inflated
            }
        } else {
            convertView.tag as ItemThoughtBinding
        }

        val item = getItem(position)
        binding.textDateTime.text = DateTimeFormatterUtil.formatFullDateTime(item.dateTimeMillis)
        binding.textSituation.text = item.situation
        binding.textAutomaticThinking.text = item.automaticThinking
        binding.textIntensity.text = item.emotionIntensity?.let {
            parent.context.getString(R.string.emotion_intensity_value, it)
        } ?: parent.context.getString(R.string.emotion_intensity_missing)
        binding.imageEmotion.contentDescription = item.emotional
        val emojiResource = EmotionImages.forLabel(item.emotional)
        binding.imageEmotion.isVisible = emojiResource != null
        if (emojiResource != null) {
            val bitmap = emotionBitmaps.getOrPut(emojiResource) {
                BitmapFactory.decodeResource(
                    parent.resources,
                    emojiResource,
                    BitmapFactory.Options().apply { inSampleSize = 4 }
                )
            }
            binding.imageEmotion.setImageBitmap(bitmap)
        } else {
            // Recycled cards must not show the previous record's emoji.
            binding.imageEmotion.setImageDrawable(null)
        }
        binding.buttonMore.setOnClickListener { view ->
            onMenuClick(view, item)
        }

        return binding.root
    }

    fun submitList(newItems: List<ThoughtRecord>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
