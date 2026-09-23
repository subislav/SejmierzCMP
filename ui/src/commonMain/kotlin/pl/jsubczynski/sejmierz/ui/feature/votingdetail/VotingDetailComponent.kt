package pl.jsubczynski.sejmierz.ui.feature.votingdetail

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.VotingDetail
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingDetailUseCase
import pl.jsubczynski.sejmierz.ui.core.componentScope

data class VotingDetailState(
    val detail: VotingDetail? = null,
    val isLoading: Boolean = true,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

interface VotingDetailComponent {
    val state: StateFlow<VotingDetailState>
    fun onBack()
    fun onRetry()
}

class DefaultVotingDetailComponent(
    componentContext: ComponentContext,
    private val sitting: Int,
    private val votingNumber: Int,
    private val getVotingDetail: GetVotingDetailUseCase,
    private val settings: SettingsRepository,
    private val onFinished: () -> Unit,
) : VotingDetailComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val _state = MutableStateFlow(VotingDetailState())
    override val state: StateFlow<VotingDetailState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onBack() = onFinished()

    override fun onRetry() = load()

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val result = getVotingDetail(
                term = _state.value.term,
                sitting = sitting,
                votingNumber = votingNumber,
                source = settings.dataSourceMode.value.toDataSource(),
            )
            _state.update {
                when (result) {
                    is DomainResult.Success -> it.copy(detail = result.data, isLoading = false)
                    is DomainResult.Failure -> it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
