package pl.jsubczynski.sejmierz.ui.feature.clubs

import kotlinx.coroutines.flow.StateFlow
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode
import pl.jsubczynski.sejmierz.domain.model.Term

data class ClubsState(
    val clubs: List<Club> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val canLoadMore: Boolean = true,
    val mode: DataSourceMode = DataSourceMode.ONLINE,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

sealed interface ClubsIntent {
    data object Refresh : ClubsIntent
    data object LoadMore : ClubsIntent
    data class OpenDetail(val id: String) : ClubsIntent
}

interface ClubsComponent {
    val state: StateFlow<ClubsState>
    fun onIntent(intent: ClubsIntent)
}