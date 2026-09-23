package pl.jsubczynski.sejmierz.ui.feature.votings

import kotlinx.coroutines.flow.StateFlow
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.Voting

data class VotingsState(
    val votings: List<Voting> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val canLoadMore: Boolean = true,
    val mode: DataSourceMode = DataSourceMode.ONLINE,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

sealed interface VotingsIntent {
    data object Refresh : VotingsIntent
    data object LoadMore : VotingsIntent
    data class OpenDetail(val sitting: Int, val votingNumber: Int) : VotingsIntent
}

interface VotingsComponent {
    val state: StateFlow<VotingsState>
    fun onIntent(intent: VotingsIntent)
}