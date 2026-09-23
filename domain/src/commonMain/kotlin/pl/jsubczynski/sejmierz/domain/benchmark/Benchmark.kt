package pl.jsubczynski.sejmierz.domain.benchmark

import kotlin.time.TimeSource

class Benchmark(
    private val sink: MetricSink,
    private val clock: () -> Long = ::currentTimeMillis,
) {


    suspend inline fun <T> measured(
        category: MetricCategory,
        name: String,
        tags: Map<String, String> = emptyMap(),
        block: () -> T,
    ): T {
        val mark = TimeSource.Monotonic.markNow()
        val result = block()
        val elapsedMs = mark.elapsedNow().inWholeMicroseconds / 1000.0
        record(name, category, elapsedMs, Metric.UNIT_MS, tags)
        return result
    }


    fun record(
        name: String,
        category: MetricCategory,
        value: Double,
        unit: String,
        tags: Map<String, String> = emptyMap(),
    ) {
        sink.record(
            Metric(
                name = name,
                category = category,
                value = value,
                unit = unit,
                timestampMs = clock(),
                tags = tags,
            ),
        )
    }
}
