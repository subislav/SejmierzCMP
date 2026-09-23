package pl.jsubczynski.sejmierz.domain.benchmark

enum class MetricCategory {
    NETWORK,
    PARSING,
    DB,
    USECASE,
    UI,
    MEMORY,
    CPU,
}

object MetricTag {
    const val PLATFORM = "platform"
    const val DEVICE = "device"
    const val OS_VERSION = "osVersion"
    const val SOURCE = "source"
    const val MODE = "mode"
    const val COLD_WARM = "coldWarm"
    const val NETWORK_TYPE = "networkType"
    const val BUILD_TYPE = "buildType"
    const val ENDPOINT = "endpoint"
    const val SLICE = "slice"
    const val RUN_ID = "runId"
    const val ITERATION = "iteration"
}

object MetricNames {
    const val UI_TTID = "ui.ttid"
    const val UI_TTFD = "ui.ttfd"
    const val UI_JANK_FRAMES = "ui.jankFrames"
    const val UI_MAIN_THREAD_STALL_COUNT = "ui.mainThreadStallCount"
    const val UI_MAIN_THREAD_STALL_MS = "ui.mainThreadStallTotalMs"
    const val APP_COLD_START = "app.coldStart"
    const val CPU_AVG = "cpu.avg"
    const val CPU_PEAK = "cpu.peak"
    const val NETWORK_TRANSPORT = "network.transport"
    const val PARSING_DECODE = "parsing.decode"
}

data class Metric(
    val name: String,
    val category: MetricCategory,
    val value: Double,
    val unit: String,
    val timestampMs: Long,
    val tags: Map<String, String> = emptyMap(),
) {
    companion object {
        const val UNIT_MS = "ms"
        const val UNIT_BYTES = "bytes"
        const val UNIT_FRAMES = "frames"
        const val UNIT_COUNT = "count"
        const val UNIT_PERCENT = "percent"
    }
}
