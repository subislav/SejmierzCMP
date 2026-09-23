package pl.jsubczynski.sejmierz.domain.benchmark

interface MemorySampler {
    fun usedMemoryBytes(): Long
}

interface DisplayTracker {
    fun reportInitialDisplay(slice: String)
    fun reportFullDisplay(slice: String)
    fun reportColdStart(elapsedMs: Double)
}

interface FrameMetricsSampler {
    fun start(slice: String)
    fun stop()
}

interface CpuSampler {
    fun start(slice: String)
    fun stop()
}

object NoopMemorySampler : MemorySampler {
    override fun usedMemoryBytes(): Long = 0L
}

object NoopDisplayTracker : DisplayTracker {
    override fun reportInitialDisplay(slice: String) = Unit
    override fun reportFullDisplay(slice: String) = Unit
    override fun reportColdStart(elapsedMs: Double) = Unit
}

object NoopFrameMetricsSampler : FrameMetricsSampler {
    override fun start(slice: String) = Unit
    override fun stop() = Unit
}

object NoopCpuSampler : CpuSampler {
    override fun start(slice: String) = Unit
    override fun stop() = Unit
}
