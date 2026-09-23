package pl.jsubczynski.sejmierz.domain.paging

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Page

class PaginatorTest {

    @Test
    fun `accumulates pages and stops when a short page is returned`() = runTest {
        val total = (1..120).toList()
        val paginator = Paginator(pageSize = 50) { offset, limit ->
            val slice = total.drop(offset).take(limit)
            DomainResult.success(Page(items = slice, offset = offset, limit = limit))
        }

        val first = paginator.loadNext().getOrNull()!!
        assertEquals(50, first.size)
        assertTrue(paginator.canLoadMore)

        paginator.loadNext()
        val third = paginator.loadNext().getOrNull()!!

        assertEquals(120, third.size)
        assertFalse(paginator.canLoadMore)
    }
}
