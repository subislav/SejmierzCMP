package pl.jsubczynski.sejmierz.ui.feature.mpdetail

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetMpDetailUseCase
import pl.jsubczynski.sejmierz.ui.core.componentScope

data class MpDetailState(
    val mp: Mp? = null,
    val isLoading: Boolean = true,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

interface MpDetailComponent {
    val state: StateFlow<MpDetailState>
    fun onBack()
    fun onRetry()
}

class DefaultMpDetailComponent(
    componentContext: ComponentContext,
    private val mpId: Int,
    private val getMpDetail: GetMpDetailUseCase,
    private val settings: SettingsRepository,
    private val onFinished: () -> Unit,
) : MpDetailComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val _state = MutableStateFlow(MpDetailState())
    override val state: StateFlow<MpDetailState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onBack() = onFinished()

    override fun onRetry() = load()

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val result = getMpDetail(
                term = _state.value.term,
                id = mpId,
                source = settings.dataSourceMode.value.toDataSource(),
            )
            _state.update {
                when (result) {
                    is DomainResult.Success -> it.copy(mp = result.data, isLoading = false)
                    is DomainResult.Failure -> it.copy(
                        isLoading = false,
                        error = result.error,
                    )
                }
            }
        }
    }
}
