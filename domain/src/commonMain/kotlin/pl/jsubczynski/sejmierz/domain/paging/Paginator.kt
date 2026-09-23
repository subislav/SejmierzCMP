package pl.jsubczynski.sejmierz.domain.paging

import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Page

class Paginator<T>(
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val fetch: suspend (offset: Int, limit: Int) -> DomainResult<Page<T>>,
) {
    private val items = mutableListOf<T>()
    private var nextOffset = 0
    private var endReached = false
    private var loading = false

    val loadedItems: List<T> get() = items.toList()
    val canLoadMore: Boolean get() = !endReached

    fun reset() {
        items.clear()
        nextOffset = 0
        endReached = false
        loading = false
    }


    suspend fun loadNext(): DomainResult<List<T>> {
        if (loading || endReached) return DomainResult.success(items.toList())
        loading = true
        return try {
            when (val result = fetch(nextOffset, pageSize)) {
                is DomainResult.Success -> {
                    val page = result.data
                    items.addAll(page.items)
                    nextOffset = page.nextOffset
                    endReached = !page.hasMore
                    DomainResult.success(items.toList())
                }
                is DomainResult.Failure -> result
            }
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            DomainResult.failure(DomainError.Unknown(e.message))
        } finally {
            loading = false
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}
