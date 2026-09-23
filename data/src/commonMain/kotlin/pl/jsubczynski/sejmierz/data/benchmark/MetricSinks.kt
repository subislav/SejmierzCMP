package pl.jsubczynski.sejmierz.data.benchmark

import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricSink
import pl.jsubczynski.sejmierz.domain.benchmark.RunContextProvider

class ConsoleMetricSink(
    private val log: (String) -> Unit = ::println,
) : MetricSink {
    override fun record(metric: Metric) {
        val tags = metric.tags.entries.joinToString(";") { "${it.key}=${it.value}" }
        log("SEJMIERZ_BENCH|${metric.category}|${metric.name}|${metric.value}|${metric.unit}|$tags")
    }
}

class CompositeMetricSink(
    private val sinks: List<MetricSink>,
) : MetricSink {
    override fun record(metric: Metric) {
        sinks.forEach { it.record(metric) }
    }
}

class EnrichingMetricSink(
    private val delegate: MetricSink,
    private val runContext: RunContextProvider,
) : MetricSink {
    override fun record(metric: Metric) {
        val merged = runContext.globalTags() + metric.tags
        delegate.record(metric.copy(tags = merged))
    }
}
