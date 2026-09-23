package pl.jsubczynski.sejmierz.ios.benchmark

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSString
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.KERN_SUCCESS
import platform.darwin.MACH_TASK_BASIC_INFO
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.darwin.mach_msg_type_number_tVar
import platform.darwin.mach_task_basic_info
import platform.darwin.mach_task_self_
import platform.darwin.natural_t
import platform.darwin.task_info
import platform.posix.CLOCKS_PER_SEC
import platform.posix.clock
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkExporter
import pl.jsubczynski.sejmierz.domain.benchmark.CpuSampler
import pl.jsubczynski.sejmierz.domain.benchmark.DisplayTracker
import pl.jsubczynski.sejmierz.domain.benchmark.FrameMetricsSampler
import pl.jsubczynski.sejmierz.domain.benchmark.MemorySampler
import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricNames
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.benchmark.RunContextProvider
import kotlin.concurrent.AtomicReference
import kotlin.time.TimeSource

private val processStartMark = TimeSource.Monotonic.markNow()

fun iosColdStartElapsedMs(): Double =
    processStartMark.elapsedNow().inWholeMicroseconds / 1000.0

@OptIn(ExperimentalForeignApi::class)
class IOSMemorySampler : MemorySampler {
    override fun usedMemoryBytes(): Long = memScoped {
        val info = alloc<mach_task_basic_info>()
        val count = alloc<mach_msg_type_number_tVar>()
        count.value = (sizeOf<mach_task_basic_info>() / sizeOf<natural_t>()).toUInt()
        val kerr = task_info(
            mach_task_self_,
            MACH_TASK_BASIC_INFO.toUInt(),
            info.ptr.reinterpret(),
            count.ptr,
        )
        if (kerr == KERN_SUCCESS) info.resident_size.toLong() else 0L
    }
}

class IOSDisplayTracker(
    private val benchmark: Benchmark,
) : DisplayTracker {
    private val start = TimeSource.Monotonic.markNow()

    override fun reportInitialDisplay(slice: String) {
        benchmark.record(
            name = MetricNames.UI_TTID,
            category = MetricCategory.UI,
            value = start.elapsedNow().inWholeMicroseconds / 1000.0,
            unit = Metric.UNIT_MS,
            tags = mapOf(MetricTag.SLICE to slice),
        )
    }

    override fun reportFullDisplay(slice: String) {
        benchmark.record(
            name = MetricNames.UI_TTFD,
            category = MetricCategory.UI,
            value = start.elapsedNow().inWholeMicroseconds / 1000.0,
            unit = Metric.UNIT_MS,
            tags = mapOf(MetricTag.SLICE to slice),
        )
    }

    override fun reportColdStart(elapsedMs: Double) {
        benchmark.record(
            name = MetricNames.APP_COLD_START,
            category = MetricCategory.UI,
            value = elapsedMs,
            unit = Metric.UNIT_MS,
            tags = mapOf(MetricTag.SLICE to "startup"),
        )
    }
}

class IOSFrameMetricsSampler(
    private val benchmark: Benchmark,
) : FrameMetricsSampler {
    private var jankFrames = 0
    private var totalFrames = 0
    private var stallCount = 0
    private var stallTotalMs = 0.0
    private var slice = ""
    private var lastElapsedMs = 0.0
    private var sessionStart = TimeSource.Monotonic.markNow()
    private var running = false
    private val stallThresholdMs = 16.0

    override fun start(slice: String) {
        this.slice = slice
        jankFrames = 0
        totalFrames = 0
        stallCount = 0
        stallTotalMs = 0.0
        sessionStart = TimeSource.Monotonic.markNow()
        lastElapsedMs = 0.0
        running = true
        scheduleTick()
    }

    override fun stop() {
        running = false
        val tags = mapOf(
            MetricTag.SLICE to slice,
            "totalFrames" to totalFrames.toString(),
        )
        benchmark.record(
            name = MetricNames.UI_JANK_FRAMES,
            category = MetricCategory.UI,
            value = jankFrames.toDouble(),
            unit = Metric.UNIT_FRAMES,
            tags = tags,
        )
        benchmark.record(
            name = MetricNames.UI_MAIN_THREAD_STALL_COUNT,
            category = MetricCategory.UI,
            value = stallCount.toDouble(),
            unit = Metric.UNIT_COUNT,
            tags = tags,
        )
        benchmark.record(
            name = MetricNames.UI_MAIN_THREAD_STALL_MS,
            category = MetricCategory.UI,
            value = stallTotalMs,
            unit = Metric.UNIT_MS,
            tags = tags,
        )
    }

    private fun scheduleTick() {
        if (!running) return
        dispatch_async(dispatch_get_main_queue()) {
            if (!running) return@dispatch_async
            val elapsedMs = sessionStart.elapsedNow().inWholeMicroseconds / 1000.0
            val deltaMs = elapsedMs - lastElapsedMs
            totalFrames++
            if (lastElapsedMs > 0.0 && deltaMs > stallThresholdMs) {
                jankFrames++
                stallCount++
                stallTotalMs += deltaMs
            }
            lastElapsedMs = elapsedMs
            platform.darwin.dispatch_after(
                dispatch_time(DISPATCH_TIME_NOW, 16_000_000),
                dispatch_get_main_queue(),
            ) { scheduleTick() }
        }
    }
}

class IOSCpuSampler(
    private val benchmark: Benchmark,
) : CpuSampler {
    private var slice = ""
    private var wallStart = TimeSource.Monotonic.markNow()
    private var cpuStartMs = 0.0
    private var peak = 0.0
    private var lastWallElapsedMs = 0.0
    private var lastCpuMs = 0.0
    private var running = false

    override fun start(slice: String) {
        this.slice = slice
        wallStart = TimeSource.Monotonic.markNow()
        cpuStartMs = cpuTimeMs()
        lastWallElapsedMs = 0.0
        lastCpuMs = cpuStartMs
        peak = 0.0
        running = true
        sampleLoop()
    }

    override fun stop() {
        if (!running) return
        running = false
        val wallMs = wallStart.elapsedNow().inWholeMilliseconds.coerceAtLeast(1).toDouble()
        val cpuDelta = (cpuTimeMs() - cpuStartMs).coerceAtLeast(0.0)
        val avg = (cpuDelta / wallMs) * 100.0
        if (avg > peak) peak = avg
        val tags = mapOf(MetricTag.SLICE to slice)
        benchmark.record(
            name = MetricNames.CPU_AVG,
            category = MetricCategory.CPU,
            value = avg.coerceIn(0.0, 100.0),
            unit = Metric.UNIT_PERCENT,
            tags = tags,
        )
        benchmark.record(
            name = MetricNames.CPU_PEAK,
            category = MetricCategory.CPU,
            value = peak.coerceIn(0.0, 100.0),
            unit = Metric.UNIT_PERCENT,
            tags = tags,
        )
    }

    private fun sampleLoop() {
        if (!running) return
        dispatch_async(dispatch_get_main_queue()) {
            if (!running) return@dispatch_async
            val wallElapsed = wallStart.elapsedNow().inWholeMilliseconds.toDouble()
            val cpuNow = cpuTimeMs()
            val wallDelta = (wallElapsed - lastWallElapsedMs).coerceAtLeast(1.0)
            val cpuDelta = (cpuNow - lastCpuMs).coerceAtLeast(0.0)
            val instant = (cpuDelta / wallDelta) * 100.0
            if (instant > peak) peak = instant
            lastWallElapsedMs = wallElapsed
            lastCpuMs = cpuNow
            platform.darwin.dispatch_after(
                dispatch_time(DISPATCH_TIME_NOW, 50_000_000),
                dispatch_get_main_queue(),
            ) { sampleLoop() }
        }
    }

    private fun cpuTimeMs(): Double = clock().toDouble() / CLOCKS_PER_SEC.toDouble() * 1000.0
}

class IOSBenchmarkExporter : BenchmarkExporter {
    override suspend fun export(fileName: String, content: String): String =
        withContext(Dispatchers.IO) {
            val path = NSHomeDirectory() + "/Documents/benchmark-exports/$fileName"
            @Suppress("CAST_NEVER_SUCCEEDS")
            (content as NSString).writeToFile(path, atomically = true, encoding = 4u, error = null)
            path
        }
}

class IOSRunContextProvider : RunContextProvider {
    private val runId: String =
        List(8) { "0123456789abcdef".random() }.joinToString("")

    private val networkType = AtomicReference("UNKNOWN")

    override fun globalTags(): Map<String, String> = mapOf(
        MetricTag.PLATFORM to "CMP-iOS",
        MetricTag.DEVICE to UIDevice.currentDevice.name,
        MetricTag.OS_VERSION to "iOS ${UIDevice.currentDevice.systemVersion}",
        MetricTag.BUILD_TYPE to "debug",
        MetricTag.NETWORK_TYPE to networkType.value,
        MetricTag.RUN_ID to runId,
    )

    fun updateNetworkType(type: String) {
        networkType.value = type
    }
}
