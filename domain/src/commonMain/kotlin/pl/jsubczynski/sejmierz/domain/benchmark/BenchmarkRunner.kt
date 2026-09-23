package pl.jsubczynski.sejmierz.domain.benchmark

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

class BenchmarkRunner(
    private val benchmark: Benchmark,
) {
    suspend fun <T> run(
        category: MetricCategory,
        name: String,
        config: BenchmarkConfig = BenchmarkConfig(),
        baseTags: Map<String, String> = emptyMap(),
        operation: suspend () -> T,
    ) {
        repeat(config.totalIterations) { iteration ->
            coroutineContext.ensureActive()
            val isWarmup = iteration < config.warmupIterations
            val tags = baseTags + mapOf(
                MetricTag.ITERATION to iteration.toString(),
                MetricTag.COLD_WARM to if (isWarmup) "WARMUP" else "CLEAN",
            )
            benchmark.measured(category, name, tags) { operation() }
        }
    }
}
