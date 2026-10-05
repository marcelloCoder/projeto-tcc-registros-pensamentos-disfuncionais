package br.com.mcoder.primeiroprojeto.ui

import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord

class ThoughtListPagination(initialLimit: Int = PAGE_SIZE) {
    var visibleLimit: Int = initialLimit.coerceAtLeast(PAGE_SIZE)
        private set
    private var previousQuery: ThoughtQuery? = null
    private var totalCount = 0

    fun page(filteredRecords: List<ThoughtRecord>, query: ThoughtQuery): Page {
        if (previousQuery != null && previousQuery != query) visibleLimit = PAGE_SIZE
        previousQuery = query
        totalCount = filteredRecords.size
        return Page(filteredRecords.take(visibleLimit), totalCount)
    }

    fun showMore() {
        if (visibleLimit < totalCount) visibleLimit += PAGE_SIZE
    }

    data class Page(val records: List<ThoughtRecord>, val totalCount: Int) {
        val remainingCount: Int get() = totalCount - records.size
        val hasMore: Boolean get() = remainingCount > 0
        val nextCount: Int get() = remainingCount.coerceAtMost(PAGE_SIZE)
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}
