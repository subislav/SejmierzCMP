package pl.jsubczynski.sejmierz.domain.benchmark

data class BenchmarkConfig(
    val totalIterations: Int = DEFAULT_TOTAL_ITERATIONS,
    val warmupIterations: Int = DEFAULT_WARMUP_ITERATIONS,
) {
    init {
        require(totalIterations > 0) { "totalIterations must be > 0" }
        require(warmupIterations in 0 until totalIterations) {
            "warmupIterations must be in 0 until totalIterations"
        }
    }


    val cleanSamples: Int get() = totalIterations - warmupIterations

    companion object {
        const val DEFAULT_TOTAL_ITERATIONS = 35
        const val DEFAULT_WARMUP_ITERATIONS = 5
    }
}
