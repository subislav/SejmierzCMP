package pl.jsubczynski.sejmierz.domain.benchmark

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.jsubczynski.sejmierz.domain.benchmark.stats.BenchmarkStatistics
import pl.jsubczynski.sejmierz.domain.benchmark.stats.summarize

class BenchmarkStatisticsTest {

    @Test
    fun `computes mean median and percentiles`() {
        val values = listOf(10.0, 20.0, 30.0, 40.0, 50.0)
        val stats = BenchmarkStatistics.from("net", slice = null, unit = Metric.UNIT_MS, values = values)

        assertEquals(5, stats.sampleCount)
        assertEquals(30.0, stats.mean, 0.0001)
        assertEquals(30.0, stats.median, 0.0001)
        assertEquals(10.0, stats.min, 0.0001)
        assertEquals(50.0, stats.max, 0.0001)
    }

    @Test
    fun `summarize drops warmup samples per group`() {
        val metrics = (1..10).map {
            Metric(
                name = "GetMpsUseCase",
                category = MetricCategory.USECASE,
                value = it.toDouble(),
                unit = Metric.UNIT_MS,
                timestampMs = it.toLong(),
            )
        }

        val summary = metrics.summarize(warmup = 5).single()


        assertEquals(5, summary.sampleCount)
        assertEquals(8.0, summary.mean, 0.0001)
    }

    @Test
    fun `summarize separates same metric name by slice tag`() {
        fun metric(slice: String, value: Double) = Metric(
            name = "ui.ttid",
            category = MetricCategory.UI,
            value = value,
            unit = Metric.UNIT_MS,
            timestampMs = 0L,
            tags = mapOf(MetricTag.SLICE to slice),
        )

        val metrics = listOf(
            metric("startup", 100.0),
            metric("MPs", 10.0),
            metric("MPs", 20.0),
        )

        val summary = metrics.summarize().sortedBy { it.slice }

        assertEquals(2, summary.size)
        val mps = summary.first { it.slice == "MPs" }
        val startup = summary.first { it.slice == "startup" }
        assertEquals(2, mps.sampleCount)
        assertEquals(15.0, mps.mean, 0.0001)
        assertEquals(1, startup.sampleCount)
        assertEquals(100.0, startup.mean, 0.0001)
    }
}
