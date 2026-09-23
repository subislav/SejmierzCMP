package pl.jsubczynski.sejmierz.ui.feature.benchmark

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkConfig
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkExporter
import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCsvExporter
import pl.jsubczynski.sejmierz.domain.benchmark.MetricRepository
import pl.jsubczynski.sejmierz.domain.benchmark.currentTimeMillis
import pl.jsubczynski.sejmierz.domain.benchmark.stats.BenchmarkStatistics
import pl.jsubczynski.sejmierz.domain.benchmark.stats.summarize
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.ui.core.componentScope

data class BenchmarkUiState(
    val config: BenchmarkConfig = BenchmarkConfig(),
    val stats: List<BenchmarkStatistics> = emptyList(),
    val sampleCount: Int = 0,
    val lastExportPath: String? = null,
)

interface BenchmarkComponent {
    val state: StateFlow<BenchmarkUiState>
    fun onBack()
    fun setIterations(total: Int, warmup: Int)
    fun exportCsv()
    fun clear()
}

class DefaultBenchmarkComponent(
    componentContext: ComponentContext,
    private val metrics: MetricRepository,
    private val settings: SettingsRepository,
    private val exporter: BenchmarkExporter,
    private val onFinished: () -> Unit,
) : BenchmarkComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val exportPath = MutableStateFlow<String?>(null)

    override val state: StateFlow<BenchmarkUiState> =
        combine(
            metrics.metrics,
            settings.benchmarkConfig,
            exportPath,
        ) { all: List<Metric>, config, path ->
            BenchmarkUiState(
                config = config,
                stats = all.summarize(warmup = config.warmupIterations),
                sampleCount = all.size,
                lastExportPath = path,
            )
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), BenchmarkUiState())

    override fun onBack() = onFinished()

    override fun setIterations(total: Int, warmup: Int) {
        runCatching { BenchmarkConfig(total, warmup) }
            .onSuccess { settings.setBenchmarkConfig(it) }
    }

    override fun exportCsv() {
        scope.launch {
            val csv = MetricCsvExporter.toCsv(metrics.snapshot())
            val fileName = "sejmierz-benchmarks-${currentTimeMillis()}.csv"
            val path = exporter.export(fileName, csv)
            exportPath.update { path }
        }
    }

    override fun clear() {
        metrics.clear()
        exportPath.update { null }
    }
}
