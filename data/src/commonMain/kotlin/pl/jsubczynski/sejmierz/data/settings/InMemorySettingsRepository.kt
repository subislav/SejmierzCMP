package pl.jsubczynski.sejmierz.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkConfig
import pl.jsubczynski.sejmierz.domain.model.AppLanguage
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository

class InMemorySettingsRepository : SettingsRepository {

    private val _dataSourceMode = MutableStateFlow(DataSourceMode.ONLINE)
    override val dataSourceMode: StateFlow<DataSourceMode> = _dataSourceMode.asStateFlow()

    private val _benchmarkConfig = MutableStateFlow(BenchmarkConfig())
    override val benchmarkConfig: StateFlow<BenchmarkConfig> = _benchmarkConfig.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.PL)
    override val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    override fun setDataSourceMode(mode: DataSourceMode) {
        _dataSourceMode.value = mode
    }

    override fun setBenchmarkConfig(config: BenchmarkConfig) {
        _benchmarkConfig.value = config
    }

    override fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }
}
