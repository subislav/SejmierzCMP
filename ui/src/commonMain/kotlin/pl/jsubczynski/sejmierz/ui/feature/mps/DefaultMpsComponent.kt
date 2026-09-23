package pl.jsubczynski.sejmierz.ui.feature.mps

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
import pl.jsubczynski.sejmierz.domain.usecase.GetMpsUseCase
import pl.jsubczynski.sejmierz.ui.core.componentScope

class DefaultMpsComponent(
    componentContext: ComponentContext,
    private val getMps: GetMpsUseCase,
    private val settings: SettingsRepository,
    private val onOpenMp: (Int) -> Unit,
    private val scope: CoroutineScope = componentContext.componentScope(),
) : MpsComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(MpsState(mode = settings.dataSourceMode.value))
    override val state: StateFlow<MpsState> = _state.asStateFlow()

    private var paginator = newPaginator()

    init {

        scope.launch {
            settings.dataSourceMode.collect { mode ->
                _state.update { it.copy(mode = mode) }
                refresh()
            }
        }
    }

    override fun onIntent(intent: MpsIntent) {
        when (intent) {
            MpsIntent.Refresh -> refresh()
            MpsIntent.LoadMore -> loadMore()
            is MpsIntent.OpenDetail -> onOpenMp(intent.id)
        }
    }

    private fun newPaginator() = Paginator(pageSize = PAGE_SIZE) { offset, limit ->
        getMps(
            term = _state.value.term,
            source = _state.value.mode.toDataSource(),
            offset = offset,
            limit = limit,
        )
    }

    private fun refresh() {
        paginator = newPaginator()
        _state.update {
            it.copy(isLoading = true, isRefreshing = it.mps.isNotEmpty(), error = null)
        }
        scope.launch {
            when (val result = paginator.loadNext()) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        mps = result.data,
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
                        mps = result.data,
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