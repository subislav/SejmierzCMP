package pl.jsubczynski.sejmierz.domain.benchmark

import kotlinx.coroutines.flow.Flow

interface MetricSink {
    fun record(metric: Metric)
}

interface MetricRepository : MetricSink {

    val metrics: Flow<List<Metric>>

    fun snapshot(): List<Metric>

    fun clear()
}
