package pl.jsubczynski.sejmierz.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkConfig
import pl.jsubczynski.sejmierz.domain.model.AppLanguage
import pl.jsubczynski.sejmierz.domain.model.DataSourceMode

interface SettingsRepository {
    val dataSourceMode: StateFlow<DataSourceMode>
    val benchmarkConfig: StateFlow<BenchmarkConfig>
    val appLanguage: StateFlow<AppLanguage>

    fun setDataSourceMode(mode: DataSourceMode)
    fun setBenchmarkConfig(config: BenchmarkConfig)
    fun setAppLanguage(language: AppLanguage)
}
