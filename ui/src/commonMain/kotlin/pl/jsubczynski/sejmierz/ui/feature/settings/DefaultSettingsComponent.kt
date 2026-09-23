package pl.jsubczynski.sejmierz.ui.feature.settings

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.ui.core.componentScope

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val settingsRepository: SettingsRepository,
    private val onGoBack: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {


    private val scope = componentScope()


    override val state: StateFlow<SettingsState> = combine(
        settingsRepository.dataSourceMode,
        settingsRepository.appLanguage
    ) { mode, language ->
        SettingsState(
            mode = mode,
            language = language
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleMode -> {
                settingsRepository.setDataSourceMode(intent.mode)
            }
            is SettingsIntent.ChangeLanguage -> {
                settingsRepository.setAppLanguage(intent.language)
            }
            SettingsIntent.GoBack -> {
                onGoBack()
            }
        }
    }
}