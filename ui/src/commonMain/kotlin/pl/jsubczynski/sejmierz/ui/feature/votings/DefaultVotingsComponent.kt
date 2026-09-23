package pl.jsubczynski.sejmierz.ui.feature.votings

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.paging.Paginator
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingsUseCase
import pl.jsubczynski.sejmierz.ui.core.componentScope

class DefaultVotingsComponent(
    componentContext: ComponentContext,
    private val getVotings: GetVotingsUseCase,
    private val settings: SettingsRepository,
    private val onOpenVoting: (sitting: Int, votingNumber: Int) -> Unit,
    private val scope: CoroutineScope = componentContext.componentScope(),
) : VotingsComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(VotingsState(mode = settings.dataSourceMode.value))
    override val state: StateFlow<VotingsState> = _state.asStateFlow()

    private var paginator = newPaginator()

    init {

        scope.launch {
            settings.dataSourceMode.collect { mode ->
                _state.update { it.copy(mode = mode) }
                refresh()
            }
        }
    }

    override fun onIntent(intent: VotingsIntent) {
        when (intent) {
            VotingsIntent.Refresh -> refresh()
            VotingsIntent.LoadMore -> loadMore()
            is VotingsIntent.OpenDetail -> onOpenVoting(intent.sitting, intent.votingNumber)
        }
    }

    private fun newPaginator() = Paginator(pageSize = PAGE_SIZE) { offset, limit ->
        getVotings(
            term = _state.value.term,
            source = _state.value.mode.toDataSource(),
            offset = offset,
            limit = limit,
        )
    }

    private fun refresh() {
        paginator = newPaginator()
        _state.update {
            it.copy(isLoading = true, isRefreshing = it.votings.isNotEmpty(), error = null)
        }
        scope.launch {
            when (val result = paginator.loadNext()) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        votings = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        canLoadMore = paginator.canLoadMore,
                        error = null,
                    )
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.error)
                }
            }
        }
    }

    private fun loadMore() {
        if (_state.value.isLoadingMore || !paginator.canLoadMore) return
        _state.update { it.copy(isLoadingMore = true) }
        scope.launch {
            when (val result = paginator.loadNext()) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        votings = result.data,
                        isLoadingMore = false,
                        canLoadMore = paginator.canLoadMore,
                    )
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(isLoadingMore = false, error = result.error)
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}