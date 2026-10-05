package br.com.mcoder.primeiroprojeto.ui

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.data.MotivationService
import br.com.mcoder.primeiroprojeto.databinding.FragmentThoughtListBinding
import br.com.mcoder.primeiroprojeto.databinding.HeaderThoughtListBinding
import br.com.mcoder.primeiroprojeto.databinding.FooterThoughtListBinding
import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.util.DateTimeFormatterUtil
import br.com.mcoder.primeiroprojeto.util.ThoughtFilterEngine
import br.com.mcoder.primeiroprojeto.util.MotivationPhrases
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ThoughtListFragment : Fragment() {
    interface Callbacks {
        fun onCreateThoughtRequested()
        fun onEditThoughtRequested(recordId: String)
    }

    private var _binding: FragmentThoughtListBinding? = null
    private val binding get() = _binding!!
    private var _headerBinding: HeaderThoughtListBinding? = null
    private val header get() = _headerBinding!!
    private var _footerBinding: FooterThoughtListBinding? = null
    private val footer get() = _footerBinding!!
    private var pagination = ThoughtListPagination()
    private lateinit var adapter: ThoughtListAdapter
    private var allThoughts: List<ThoughtRecord> = emptyList()
    private var currentSearchTerm: String = ""
    private var currentEmotion: String = ThoughtQuery.ALL_EMOTIONS
    private var dateFrom: LocalDate? = null
    private var dateTo: LocalDate? = null
    private val motivationPhrases = MotivationPhrases()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThoughtListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val periodState = savedInstanceState ?: arguments
        dateFrom = periodState?.getString(STATE_FROM)?.let(LocalDate::parse)
        dateTo = periodState?.getString(STATE_TO)?.let(LocalDate::parse)
        currentSearchTerm = savedInstanceState?.getString(STATE_SEARCH).orEmpty()
        currentEmotion = savedInstanceState?.getString(STATE_EMOTION) ?: ThoughtQuery.ALL_EMOTIONS
        pagination = ThoughtListPagination(savedInstanceState?.getInt(STATE_VISIBLE_LIMIT) ?: ThoughtListPagination.PAGE_SIZE)

        _headerBinding = HeaderThoughtListBinding.inflate(layoutInflater, binding.listThoughts, false)
        binding.listThoughts.addHeaderView(header.root, null, false)
        (binding.layoutEmpty.parent as ViewGroup).removeView(binding.layoutEmpty)
        _footerBinding = FooterThoughtListBinding.inflate(layoutInflater, binding.listThoughts, false)
        footer.emptyContainer.addView(binding.layoutEmpty)
        binding.listThoughts.addFooterView(footer.root, null, false)
        footer.buttonShowMore.setOnClickListener {
            val position = binding.listThoughts.firstVisiblePosition
            val offset = binding.listThoughts.getChildAt(0)?.top ?: 0
            pagination.showMore()
            applyFilters()
            binding.listThoughts.setSelectionFromTop(position, offset)
        }

        adapter = ThoughtListAdapter(requireContext(), ::showItemMenu)
        binding.listThoughts.adapter = adapter
        binding.listThoughts.setOnItemClickListener { parent, _, position, _ ->
            val record = parent.getItemAtPosition(position) as? ThoughtRecord
            record?.let(::showRecordDetail)
        }

        header.buttonQuickAdd.setOnClickListener {
            (activity as? Callbacks)?.onCreateThoughtRequested()
        }
        header.buttonClearPeriod.setOnClickListener {
            dateFrom = null
            dateTo = null
            applyFilters()
        }

        header.searchThoughts.setQuery(currentSearchTerm, false)
        header.searchThoughts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchTerm = query.orEmpty()
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchTerm = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        header.spinnerEmotion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentEmotion = parent?.getItemAtPosition(position)?.toString()
                    ?: ThoughtQuery.ALL_EMOTIONS
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        startMotivationUpdates()
    }

    private fun startMotivationUpdates() {
        motivationPhrases.current?.let {
            header.textMotivation.text = it
            header.imageMotivationAi.contentDescription = getString(R.string.motivation_ai_icon)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) {
                    delay(motivationPhrases.waitMillis(SystemClock.elapsedRealtime()))
                    try {
                        if (motivationPhrases.needsRefill) {
                            header.imageMotivationAi.contentDescription = getString(R.string.motivation_loading)
                            motivationPhrases.refill(withTimeout(30_000L) {
                                MotivationService.generatePhrases()
                            })
                        }
                        header.textMotivation.text = motivationPhrases.advance(SystemClock.elapsedRealtime())
                        header.imageMotivationAi.contentDescription = getString(R.string.motivation_ai_icon)
                    } catch (error: TimeoutCancellationException) {
                        showMotivationFailure(error)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        showMotivationFailure(error)
                    }
                }
            }
        }
    }

    private fun showMotivationFailure(error: Exception) {
        // Avoid logging response content, account details or credentials.
        Log.w(TAG, "Motivation request failed: ${error.javaClass.simpleName}")
        header.imageMotivationAi.contentDescription = getString(R.string.motivation_unavailable)
        Snackbar.make(binding.root, R.string.motivation_unavailable, Snackbar.LENGTH_LONG).show()
        motivationPhrases.retryLater(SystemClock.elapsedRealtime())
    }

    override fun onResume() {
        super.onResume()
        reloadThoughts()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_FROM, dateFrom?.toString())
        outState.putString(STATE_TO, dateTo?.toString())
        outState.putString(STATE_SEARCH, currentSearchTerm)
        outState.putString(STATE_EMOTION, currentEmotion)
        outState.putInt(STATE_VISIBLE_LIMIT, pagination.visibleLimit)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _headerBinding = null
        _footerBinding = null
        _binding = null
    }

    private fun reloadThoughts() {
        val user = AppGraph.authRepository.getCurrentUser() ?: return
        allThoughts = AppGraph.thoughtRepository.getThoughtsForCurrentUser()

        val firstName = user.name.substringBefore(" ").ifBlank { user.name }
        header.textGreeting.text = getString(R.string.greeting_with_name, firstName)
        header.textThoughtCount.text = allThoughts.size.toString()
        header.textLatestValue.text = allThoughts.maxByOrNull { it.dateTimeMillis }
            ?.let { DateTimeFormatterUtil.formatDate(it.dateTimeMillis) }
            ?: getString(R.string.no_entries_yet)

        updateEmotionOptions()
        applyFilters()
    }

    private fun updateEmotionOptions() {
        val emotionOptions = ThoughtFilterEngine.emotionOptions(allThoughts)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            emotionOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        header.spinnerEmotion.adapter = spinnerAdapter

        val selection = emotionOptions.indexOf(currentEmotion).takeIf { it >= 0 } ?: 0
        if (header.spinnerEmotion.selectedItemPosition != selection) {
            header.spinnerEmotion.setSelection(selection, false)
        }
        currentEmotion = emotionOptions[selection]
    }

    private fun applyFilters() {
        val query = ThoughtQuery(
                searchTerm = currentSearchTerm,
                emotion = currentEmotion,
                dateFrom = dateFrom,
                dateTo = dateTo
        )
        val filteredThoughts = ThoughtFilterEngine.apply(allThoughts, query)
        val page = pagination.page(filteredThoughts, query)
        binding.layoutEmpty.isVisible = filteredThoughts.isEmpty()
        footer.emptyContainer.isVisible = filteredThoughts.isEmpty()
        footer.layoutPagination.isVisible = filteredThoughts.isNotEmpty()
        footer.buttonShowMore.isVisible = page.hasMore
        if (page.hasMore) {
            footer.textRemaining.text = resources.getQuantityString(R.plurals.entries_remaining, page.remainingCount, page.remainingCount)
            footer.buttonShowMore.contentDescription = resources.getQuantityString(
                R.plurals.show_more_entries_accessibility, page.nextCount, page.nextCount
            )
        } else {
            footer.textRemaining.setText(R.string.entries_all_shown)
        }
        adapter.submitList(page.records)
        val hasPeriod = dateFrom != null && dateTo != null
        header.textPeriodFilter.isVisible = hasPeriod
        header.buttonClearPeriod.isVisible = hasPeriod
        if (hasPeriod) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
            header.textPeriodFilter.text = getString(
                R.string.period_active, dateFrom?.format(formatter), dateTo?.format(formatter)
            )
        }
        binding.textEmptyTitle.setText(
            if (allThoughts.isEmpty()) R.string.empty_state_title else R.string.period_no_results
        )
        binding.textEmptyBody.setText(
            if (allThoughts.isEmpty()) R.string.empty_state_body else R.string.period_no_results_hint
        )
        header.textResults.text = resources.getQuantityString(
            R.plurals.entries_shown, page.totalCount, page.records.size, page.totalCount
        )
    }

    private fun showItemMenu(anchor: View, record: ThoughtRecord) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_thought_actions, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_view -> {
                        showRecordDetail(record)
                        true
                    }

                    R.id.action_edit -> {
                        (activity as? Callbacks)?.onEditThoughtRequested(record.id)
                        true
                    }

                    R.id.action_delete -> {
                        confirmDelete(record)
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun showRecordDetail(record: ThoughtRecord) {
        val detailMessage = getString(
            R.string.record_detail_template,
            DateTimeFormatterUtil.formatFullDateTime(record.dateTimeMillis),
            record.situation,
            record.automaticThinking,
            record.emotional,
            record.emotionIntensity?.let { getString(R.string.emotion_intensity_value, it) }
                ?: getString(R.string.emotion_intensity_missing),
            record.distressingSensation.ifBlank { getString(R.string.situation_sensation_missing) },
            record.thoughtBelief?.let { getString(R.string.thought_belief_value, it) }
                ?: getString(R.string.thought_belief_missing)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.record_detail_title)
            .setMessage(detailMessage)
            .setPositiveButton(R.string.edit) { _, _ ->
                (activity as? Callbacks)?.onEditThoughtRequested(record.id)
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                confirmDelete(record)
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private fun confirmDelete(record: ThoughtRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_entry_title)
            .setMessage(R.string.delete_entry_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                val result = AppGraph.thoughtRepository.deleteThought(record.id)
                result.onSuccess {
                    reloadThoughts()
                    Snackbar.make(binding.root, R.string.entry_deleted, Snackbar.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Snackbar.make(binding.root, error.message.orEmpty(), Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        const val TAG = "ThoughtListFragment"
        private const val STATE_FROM = "period_from"
        private const val STATE_TO = "period_to"
        private const val STATE_SEARCH = "search_term"
        private const val STATE_EMOTION = "emotion_filter"
        private const val STATE_VISIBLE_LIMIT = "visible_limit"

        fun newInstance(from: LocalDate, to: LocalDate) = ThoughtListFragment().apply {
            arguments = Bundle().apply {
                putString(STATE_FROM, from.toString())
                putString(STATE_TO, to.toString())
            }
        }
    }
}
