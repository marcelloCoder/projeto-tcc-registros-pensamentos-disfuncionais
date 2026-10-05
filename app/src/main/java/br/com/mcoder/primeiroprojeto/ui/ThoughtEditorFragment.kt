package br.com.mcoder.primeiroprojeto.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.databinding.FragmentThoughtEditorBinding
import br.com.mcoder.primeiroprojeto.databinding.ItemEmotionBinding
import br.com.mcoder.primeiroprojeto.databinding.ItemIntensityBinding
import br.com.mcoder.primeiroprojeto.model.EmotionIntensity
import br.com.mcoder.primeiroprojeto.model.EmotionOption
import br.com.mcoder.primeiroprojeto.model.EmotionSelection
import br.com.mcoder.primeiroprojeto.model.ThoughtDraft
import br.com.mcoder.primeiroprojeto.model.ThoughtBelief
import br.com.mcoder.primeiroprojeto.model.ThoughtDateTimeSelection
import br.com.mcoder.primeiroprojeto.util.DateTimeFormatterUtil
import br.com.mcoder.primeiroprojeto.notifications.AppNotifications
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import kotlin.math.roundToInt

class ThoughtEditorFragment : Fragment() {
    interface Callbacks {
        fun onThoughtSaved()
        fun onThoughtDeleted()
    }

    val recordId: String?
        get() = arguments?.getString(ARG_RECORD_ID)

    private var _binding: FragmentThoughtEditorBinding? = null
    private val binding get() = _binding!!
    private var dateTimeSelection = ThoughtDateTimeSelection()
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockTick = object : Runnable {
        override fun run() {
            if (_binding == null) return
            updateDateTimeButtons()
            clockHandler.postDelayed(this, 1_000L)
        }
    }
    private var emotionSelection = EmotionSelection()
    private var selectedIntensity: Int? = null
    private var selectedBelief: Int? = null
    private val emotionCards = mutableMapOf<EmotionOption, ItemEmotionBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThoughtEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonPickDate.setOnClickListener { openDatePicker() }
        binding.buttonPickTime.setOnClickListener { openTimePicker() }
        binding.buttonSave.setOnClickListener { saveThought() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }

        val record = recordId?.let(AppGraph.thoughtRepository::getThought)
        val fixedTime = if (savedInstanceState?.containsKey(STATE_FIXED_TIME) == true) {
            savedInstanceState.getLong(STATE_FIXED_TIME)
        } else {
            record?.dateTimeMillis
        }
        dateTimeSelection = ThoughtDateTimeSelection(fixedTime)
        if (record != null) {
            binding.inputSituation.setText(record.situation)
            binding.inputDistressingSensation.setText(record.distressingSensation)
            binding.inputAutomaticThinking.setText(record.automaticThinking)
        }

        emotionSelection = EmotionSelection(
            savedInstanceState?.getString(STATE_EMOTION) ?: record?.emotional
        )
        setupEmotionPicker()
        selectedIntensity = if (savedInstanceState?.containsKey(STATE_INTENSITY) == true) {
            savedInstanceState.getInt(STATE_INTENSITY).takeIf { EmotionIntensity.isValid(it) }
        } else {
            record?.emotionIntensity
        }
        setupIntensityPicker()
        selectedBelief = if (savedInstanceState?.containsKey(STATE_BELIEF) == true) {
            savedInstanceState.getInt(STATE_BELIEF).takeIf { ThoughtBelief.isValid(it) }
        } else {
            record?.thoughtBelief
        }
        setupBeliefPicker()

        binding.textScreenTitle.text = getString(
            if (record == null) R.string.new_entry_title else R.string.edit_entry_title
        )
        binding.textScreenBody.text = getString(
            if (record == null) R.string.new_entry_body else R.string.edit_entry_body
        )
        binding.buttonDelete.isVisible = record != null
        updateDateTimeButtons()
    }

    override fun onResume() {
        super.onResume()
        clockHandler.removeCallbacks(clockTick)
        clockTick.run()
    }

    override fun onPause() {
        clockHandler.removeCallbacks(clockTick)
        super.onPause()
    }

    override fun onDestroyView() {
        clockHandler.removeCallbacks(clockTick)
        super.onDestroyView()
        emotionCards.clear()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_EMOTION, emotionSelection.value)
        outState.putInt(STATE_INTENSITY, selectedIntensity ?: -1)
        outState.putInt(STATE_BELIEF, selectedBelief ?: -1)
        dateTimeSelection.fixedTimeMillis?.let { outState.putLong(STATE_FIXED_TIME, it) }
    }

    private fun setupBeliefPicker() {
        ThoughtBelief.range.forEach { belief ->
            val chip = ItemIntensityBinding.inflate(layoutInflater, binding.chipGroupBelief, false).root
            chip.id = View.generateViewId()
            chip.tag = belief
            chip.text = belief.toString()
            chip.contentDescription = getString(R.string.thought_belief_accessibility, belief)
            binding.chipGroupBelief.addView(chip)
            chip.isChecked = selectedBelief == belief
        }
        binding.chipGroupBelief.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedBelief = checkedIds.firstOrNull()?.let { id ->
                group.findViewById<Chip>(id).tag as Int
            }
            binding.textBeliefError.isVisible = false
        }
    }

    private fun setupIntensityPicker() {
        EmotionIntensity.range.forEach { intensity ->
            val chip = ItemIntensityBinding.inflate(layoutInflater, binding.chipGroupIntensity, false).root
            chip.id = View.generateViewId()
            chip.tag = intensity
            chip.text = intensity.toString()
            chip.contentDescription = getString(R.string.emotion_intensity_accessibility, intensity)
            binding.chipGroupIntensity.addView(chip)
            chip.isChecked = selectedIntensity == intensity
        }
        binding.chipGroupIntensity.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedIntensity = checkedIds.firstOrNull()?.let { id ->
                group.findViewById<Chip>(id).tag as Int
            }
            binding.textIntensityError.isVisible = false
        }
    }

    private fun setupEmotionPicker() {
        EmotionOption.entries.forEachIndexed { index, option ->
            val item = ItemEmotionBinding.inflate(layoutInflater, binding.emotionGrid, false)
            item.root.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(index / binding.emotionGrid.columnCount, GridLayout.FILL),
                GridLayout.spec(index % binding.emotionGrid.columnCount, 1f)
            ).apply {
                width = 0
                height = item.root.layoutParams.height
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
            item.imageEmotion.setImageBitmap(EmotionImages.forPicker(resources, option))
            item.textEmotionLabel.text = option.label
            item.root.contentDescription = option.label
            item.root.isSaveEnabled = false
            item.root.setOnClickListener {
                emotionSelection.select(option)
                binding.textEmotionError.isVisible = false
                updateEmotionSelection()
            }
            emotionCards[option] = item
            binding.emotionGrid.addView(item.root)
        }
        updateEmotionSelection()
    }

    private fun updateEmotionSelection() {
        emotionCards.forEach { (option, item) ->
            val selected = emotionSelection.selectedOption == option
            item.root.isChecked = selected
            item.root.strokeWidth = dp(if (selected) 2 else 1)
            item.root.strokeColor = ContextCompat.getColor(
                requireContext(), if (selected) R.color.primary else R.color.stroke_soft
            )
            item.root.setCardBackgroundColor(ContextCompat.getColor(
                requireContext(), if (selected) R.color.chip_surface else R.color.surface_primary
            ))
        }
        binding.textEmotionSelection.isVisible = emotionSelection.isValid()
        binding.textEmotionSelection.text = getString(
            if (emotionSelection.selectedOption == null) R.string.emotion_legacy
            else R.string.emotion_selected,
            emotionSelection.value
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun saveThought() {
        clearErrors()
        var hasError = false

        if (binding.inputSituation.text.isNullOrBlank()) {
            binding.inputLayoutSituation.error = getString(R.string.required_field)
            hasError = true
        }
        if (binding.inputAutomaticThinking.text.isNullOrBlank()) {
            binding.inputLayoutAutomaticThinking.error = getString(R.string.required_field)
            hasError = true
        }
        if (!ThoughtBelief.isValid(selectedBelief)) {
            binding.textBeliefError.isVisible = true
            hasError = true
        }
        if (!emotionSelection.isValid()) {
            binding.textEmotionError.isVisible = true
            hasError = true
        }
        if (!EmotionIntensity.isValid(selectedIntensity)) {
            binding.textIntensityError.isVisible = true
            hasError = true
        }
        if (hasError) {
            return
        }

        val result = AppGraph.thoughtRepository.saveThought(
            ThoughtDraft(
                id = recordId,
                situation = binding.inputSituation.text?.toString().orEmpty(),
                distressingSensation = binding.inputDistressingSensation.text?.toString().orEmpty(),
                automaticThinking = binding.inputAutomaticThinking.text?.toString().orEmpty(),
                thoughtBelief = selectedBelief,
                emotional = emotionSelection.value,
                emotionIntensity = selectedIntensity,
                dateTimeMillis = dateTimeSelection.currentTimeMillis()
            )
        )

        result.onSuccess { savedRecord ->
            AppNotifications(requireContext()).celebrateRecord(recordId, savedRecord.id)
            Snackbar.make(binding.root, R.string.entry_saved, Snackbar.LENGTH_SHORT).show()
            (activity as? Callbacks)?.onThoughtSaved()
        }.onFailure { error ->
            Snackbar.make(binding.root, error.message.orEmpty(), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun confirmDelete() {
        val currentRecordId = recordId ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_entry_title)
            .setMessage(R.string.delete_entry_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                val result = AppGraph.thoughtRepository.deleteThought(currentRecordId)
                result.onSuccess {
                    (activity as? Callbacks)?.onThoughtDeleted()
                }.onFailure { error ->
                    Snackbar.make(binding.root, error.message.orEmpty(), Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeSelection.currentTimeMillis()
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dateTimeSelection.select(calendar.timeInMillis)
                updateDateTimeButtons()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeSelection.currentTimeMillis()
        }
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                dateTimeSelection.select(calendar.timeInMillis)
                updateDateTimeButtons()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeButtons() {
        val timestamp = dateTimeSelection.currentTimeMillis()
        binding.buttonPickDate.text = DateTimeFormatterUtil.formatDate(timestamp)
        binding.buttonPickTime.text = DateTimeFormatterUtil.formatTime(timestamp)
    }

    private fun clearErrors() {
        binding.inputLayoutSituation.error = null
        binding.inputLayoutAutomaticThinking.error = null
        binding.textBeliefError.isVisible = false
        binding.textEmotionError.isVisible = false
        binding.textIntensityError.isVisible = false
    }

    companion object {
        private const val ARG_RECORD_ID = "record_id"
        private const val STATE_EMOTION = "selected_emotion"
        private const val STATE_INTENSITY = "selected_intensity"
        private const val STATE_BELIEF = "selected_belief"
        private const val STATE_FIXED_TIME = "fixed_date_time"
        const val TAG = "ThoughtEditorFragment"

        fun newInstance(recordId: String? = null): ThoughtEditorFragment {
            return ThoughtEditorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECORD_ID, recordId)
                }
            }
        }
    }
}
