package pl.jsubczynski.sejmierz.data.benchmark

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricRepository

class InMemoryMetricRepository : MetricRepository {

    private val state = MutableStateFlow<List<Metric>>(emptyList())

    override val metrics: Flow<List<Metric>> = state.asStateFlow()

    override fun record(metric: Metric) {
        state.update { it + metric }
    }

    override fun snapshot(): List<Metric> = state.value

    override fun clear() {
        state.value = emptyList()
    }
}
