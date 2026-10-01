package br.com.mcoder.primeiroprojeto.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.databinding.FragmentThoughtSummaryBinding
import br.com.mcoder.primeiroprojeto.databinding.ItemSummaryWeekBinding
import br.com.mcoder.primeiroprojeto.util.ThoughtSummaryEngine
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ThoughtSummaryFragment : Fragment() {
    interface Callbacks {
        fun onViewPeriodRequested(from: LocalDate, to: LocalDate)
    }

    private var _binding: FragmentThoughtSummaryBinding? = null
    private val binding get() = _binding!!
    private var from = LocalDate.now().minusDays(29)
    private var to = LocalDate.now()
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentThoughtSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            from = LocalDate.ofEpochDay(savedInstanceState.getLong(STATE_FROM, from.toEpochDay()))
            to = LocalDate.ofEpochDay(savedInstanceState.getLong(STATE_TO, to.toEpochDay()))
        }
        binding.buttonDateFrom.setOnClickListener { selectDate(true) }
        binding.buttonDateTo.setOnClickListener { selectDate(false) }
        binding.buttonLast30Days.setOnClickListener {
            to = LocalDate.now()
            from = to.minusDays(29)
            renderSummary()
        }
        binding.buttonViewRecords.setOnClickListener {
            (activity as? Callbacks)?.onViewPeriodRequested(from, to)
        }
    }

    override fun onResume() {
        super.onResume()
        renderSummary()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_FROM, from.toEpochDay())
        outState.putLong(STATE_TO, to.toEpochDay())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectDate(start: Boolean) {
        val selected = if (start) from else to
        DatePickerDialog(requireContext(), { _, year, month, day ->
            if (_binding == null) return@DatePickerDialog
            val date = LocalDate.of(year, month + 1, day)
            if ((start && date.isAfter(to)) || (!start && date.isBefore(from))) {
                Snackbar.make(binding.root, R.string.period_invalid, Snackbar.LENGTH_LONG).show()
            } else {
                if (start) from = date else to = date
                renderSummary()
            }
        }, selected.year, selected.monthValue - 1, selected.dayOfMonth).show()
    }

    private fun renderSummary() {
        val summary = ThoughtSummaryEngine.summarize(
            AppGraph.thoughtRepository.getThoughtsForCurrentUser(), from, to
        )
        binding.buttonDateFrom.text = getString(R.string.summary_from, from.format(formatter))
        binding.buttonDateTo.text = getString(R.string.summary_to, to.format(formatter))
        binding.textPeriod.text = getString(R.string.summary_period_value, from.format(formatter), to.format(formatter))
        binding.textTotal.text = summary.total.toString()
        binding.textFrequentTitle.setText(
            if (summary.mostFrequentEmotions.size > 1) R.string.summary_tied else R.string.summary_frequent
        )
        binding.textFrequentEmotion.text = summary.mostFrequentEmotions.joinToString(", ")
            .ifEmpty { getString(R.string.summary_no_emotion) }
        binding.textEmotionCount.isVisible = summary.emotionCount > 0
        binding.textEmotionCount.text = resources.getQuantityString(
            R.plurals.summary_occurrences, summary.emotionCount, summary.emotionCount
        )
        binding.textEmpty.isVisible = summary.total == 0
        binding.buttonViewRecords.isEnabled = summary.total > 0
        binding.layoutWeeks.removeAllViews()
        summary.weeks.forEach { week ->
            val row = ItemSummaryWeekBinding.inflate(layoutInflater, binding.layoutWeeks, false)
            row.textWeekPeriod.text = getString(
                R.string.summary_period_value, week.from.format(formatter), week.to.format(formatter)
            )
            row.textWeekCount.text = resources.getQuantityString(R.plurals.summary_week_count, week.count, week.count)
            binding.layoutWeeks.addView(row.root)
        }
    }

    companion object {
        const val TAG = "ThoughtSummaryFragment"
        private const val STATE_FROM = "summary_from"
        private const val STATE_TO = "summary_to"
    }
}
