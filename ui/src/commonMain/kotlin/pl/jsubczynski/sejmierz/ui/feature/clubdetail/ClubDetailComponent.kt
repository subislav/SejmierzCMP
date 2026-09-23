package pl.jsubczynski.sejmierz.ui.feature.clubdetail

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.jsubczynski.sejmierz.domain.error.DomainError
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.ClubDetail
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.usecase.GetClubDetailUseCase
import pl.jsubczynski.sejmierz.ui.core.componentScope

data class ClubDetailState(
    val detail: ClubDetail? = null,
    val isLoading: Boolean = true,
    val term: Term = Term.CURRENT,
    val error: DomainError? = null,
)

interface ClubDetailComponent {
    val state: StateFlow<ClubDetailState>
    fun onBack()
    fun onRetry()
}

class DefaultClubDetailComponent(
    componentContext: ComponentContext,
    private val clubId: String,
    private val getClubDetail: GetClubDetailUseCase,
    private val settings: SettingsRepository,
    private val onFinished: () -> Unit,
) : ClubDetailComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val _state = MutableStateFlow(ClubDetailState())
    override val state: StateFlow<ClubDetailState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onBack() = onFinished()

    override fun onRetry() = load()

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val result = getClubDetail(
                term = _state.value.term,
                id = clubId,
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
