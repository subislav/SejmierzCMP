package pl.jsubczynski.sejmierz.domain.benchmark.stats

import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import kotlin.math.sqrt

data class BenchmarkStatistics(
    val name: String,
    val slice: String?,
    val unit: String,
    val sampleCount: Int,
    val mean: Double,
    val median: Double,
    val min: Double,
    val max: Double,
    val p90: Double,
    val p95: Double,
    val stdDev: Double,
) {
    companion object {
        fun from(
            name: String,
            slice: String?,
            unit: String,
            values: List<Double>,
        ): BenchmarkStatistics {
            require(values.isNotEmpty()) { "Cannot compute statistics over an empty sample" }
            val sorted = values.sorted()
            val n = sorted.size
            val mean = sorted.sum() / n
            val variance = sorted.sumOf { (it - mean) * (it - mean) } / n
            return BenchmarkStatistics(
                name = name,
                slice = slice,
                unit = unit,
                sampleCount = n,
                mean = mean,
                median = percentile(sorted, 50.0),
                min = sorted.first(),
                max = sorted.last(),
                p90 = percentile(sorted, 90.0),
                p95 = percentile(sorted, 95.0),
                stdDev = sqrt(variance),
            )
        }


        private fun percentile(sorted: List<Double>, p: Double): Double {
            if (sorted.size == 1) return sorted.first()
            val rank = (p / 100.0) * (sorted.size - 1)
            val lower = rank.toInt()
            val upper = minOf(lower + 1, sorted.size - 1)
            val weight = rank - lower
            return sorted[lower] * (1 - weight) + sorted[upper] * weight
        }
    }
}

fun List<Metric>.summarize(warmup: Int = 0): List<BenchmarkStatistics> =
    groupBy { it.name to it.tags[MetricTag.SLICE] }
        .mapNotNull { (key, group) ->
            val (name, slice) = key
            val values = group.drop(warmup).map { it.value }
            if (values.isEmpty()) null
            else BenchmarkStatistics.from(name, slice, group.first().unit, values)
        }
        .sortedWith(compareBy({ it.name }, { it.slice ?: "" }))
