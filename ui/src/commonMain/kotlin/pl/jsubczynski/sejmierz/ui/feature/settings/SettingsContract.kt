package pl.jsubczynski.sejmierz.ui.feature.settings

import kotlinx.coroutines.flow.StateFlow
import pl.jsubczynski.sejmierz.domain.model.AppLanguage
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode

data class SettingsState(
    val mode: DataSourceMode = DataSourceMode.ONLINE,
    val language: AppLanguage = AppLanguage.PL,
)

sealed interface SettingsIntent {
    data class ToggleMode(val mode: DataSourceMode) : SettingsIntent
    data class ChangeLanguage(val language: AppLanguage) : SettingsIntent
    data object GoBack : SettingsIntent
}

interface SettingsComponent {
    val state: StateFlow<SettingsState>
    fun onIntent(intent: SettingsIntent)
}