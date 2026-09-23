package pl.jsubczynski.sejmierz.domain.benchmark

class BenchmarkContext(
    val benchmark: Benchmark,
    val memory: MemorySampler,
    val frames: FrameMetricsSampler,
    val display: DisplayTracker,
    val cpu: CpuSampler,
) {

    suspend inline fun <T> measuredWithMemory(
        category: MetricCategory,
        name: String,
        tags: Map<String, String> = emptyMap(),
        block: () -> T,
    ): T {
        val slice = tags[MetricTag.SLICE] ?: name
        cpu.start(slice)
        val before = memory.usedMemoryBytes()
        return try {
            val result = benchmark.measured(category, name, tags, block)
            val after = memory.usedMemoryBytes()
            benchmark.record(
                name = "$name.memoryDelta",
                category = MetricCategory.MEMORY,
                value = (after - before).toDouble(),
                unit = Metric.UNIT_BYTES,
                tags = tags,
            )
            benchmark.record(
                name = "$name.memoryUsed",
                category = MetricCategory.MEMORY,
                value = after.toDouble(),
                unit = Metric.UNIT_BYTES,
                tags = tags,
            )
            result
        } finally {
            cpu.stop()
        }
    }
}
