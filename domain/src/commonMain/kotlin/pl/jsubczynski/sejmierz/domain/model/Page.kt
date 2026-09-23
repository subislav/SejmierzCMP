package pl.jsubczynski.sejmierz.domain.model

data class Page<out T>(
    val items: List<T>,
    val offset: Int,
    val limit: Int,
) {
    val hasMore: Boolean get() = items.size >= limit
    val nextOffset: Int get() = offset + items.size
}
