package pl.jsubczynski.sejmierz.ui.feature.mps

import kotlinx.coroutines.flow.StateFlow
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Term

data class MpsState(
    val mps: List<Mp> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val canLoadMore: Boolean = true,
    val mode: DataSourceMode = DataSourceMode.ONLINE,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

sealed interface MpsIntent {
    data object Refresh : MpsIntent
    data object LoadMore : MpsIntent
    data class OpenDetail(val id: Int) : MpsIntent
}

interface MpsComponent {
    val state: StateFlow<MpsState>
    fun onIntent(intent: MpsIntent)
}