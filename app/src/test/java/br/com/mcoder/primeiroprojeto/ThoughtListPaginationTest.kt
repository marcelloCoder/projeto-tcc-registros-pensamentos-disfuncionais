package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.model.ThoughtQuery
import br.com.mcoder.primeiroprojeto.model.ThoughtRecord
import br.com.mcoder.primeiroprojeto.ui.ThoughtListPagination
import br.com.mcoder.primeiroprojeto.util.ThoughtFilterEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ThoughtListPaginationTest {
    private val records = (1..12).map { index ->
        ThoughtRecord("record-$index", "user", "Situação $index", "Pensamento", "Alegria", index.toLong(), 0L, 0L)
    }
    private val query = ThoughtQuery()

    @Test fun initiallyShowsFiveInTheSameOrderAndReportsRemainingRecords() {
        val page = ThoughtListPagination().page(records, query)
        assertEquals(records.take(5), page.records)
        assertEquals(12, page.totalCount)
        assertEquals(7, page.remainingCount)
        assertEquals(5, page.nextCount)
        assertTrue(page.hasMore)
    }

    @Test fun revealsFiveAtATimeAndHandlesPartialFinalPage() {
        val pagination = ThoughtListPagination()
        pagination.page(records, query)
        pagination.showMore()
        val secondPage = pagination.page(records, query)
        assertEquals(records.take(10), secondPage.records)
        assertEquals(2, secondPage.nextCount)
        pagination.showMore()
        val finalPage = pagination.page(records, query)
        assertEquals(records, finalPage.records)
        assertFalse(finalPage.hasMore)
        val limit = pagination.visibleLimit
        pagination.showMore()
        assertEquals(limit, pagination.visibleLimit)
    }

    @Test fun shortAndEmptyListsDoNotOfferShowMore() {
        listOf(emptyList(), records.take(1), records.take(5)).forEach {
            val page = ThoughtListPagination().page(it, query)
            assertEquals(it, page.records)
            assertFalse(page.hasMore)
            assertEquals(0, page.remainingCount)
        }
    }

    @Test fun unchangedQueryKeepsExpandedListDuringRefresh() {
        val pagination = ThoughtListPagination()
        pagination.page(records, query)
        pagination.showMore()
        assertEquals(10, pagination.page(records, query.copy()).records.size)
    }

    @Test fun searchEmotionAndPeriodChangesEachRestartAtFive() {
        listOf(ThoughtQuery(searchTerm = "Situação"), ThoughtQuery(emotion = "Alegria"),
            ThoughtQuery(dateFrom = LocalDate.of(2026, 10, 1))).forEach { newQuery ->
            val pagination = ThoughtListPagination()
            pagination.page(records, query)
            pagination.showMore()
            assertEquals(5, pagination.page(records, newQuery).records.size)
        }
    }

    @Test fun searchesEntireDiaryBeforeLimitingVisibleResults() {
        val search = ThoughtQuery(searchTerm = "Situação 12")
        val filtered = ThoughtFilterEngine.apply(records, search)
        val page = ThoughtListPagination().page(filtered, search)
        assertEquals(listOf(records.last()), page.records)
        assertFalse(page.hasMore)
    }

    @Test fun restoresExpandedLimitAfterRecreation() {
        val pagination = ThoughtListPagination()
        pagination.page(records, query)
        pagination.showMore()
        val restored = ThoughtListPagination(pagination.visibleLimit)
        assertEquals(10, restored.page(records, query).records.size)
    }

    @Test fun removingRecordsDoesNotLeavePhantomPagesOrNegativeRemainingCount() {
        val pagination = ThoughtListPagination(10)
        pagination.page(records, query)
        val page = pagination.page(records.take(4), query)
        assertEquals(4, page.records.size)
        assertEquals(0, page.remainingCount)
        assertFalse(page.hasMore)
    }
}
