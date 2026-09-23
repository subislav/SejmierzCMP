package pl.jsubczynski.sejmierz.app.benchmark

import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.view.Window
import androidx.metrics.performance.JankStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import java.io.File
import kotlin.time.TimeSource

private const val STALL_THRESHOLD_NS = 16_000_000L

class AndroidMemorySampler : MemorySampler {
    override fun usedMemoryBytes(): Long = Debug.getPss() * 1024L
}

class AndroidDisplayTracker(
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

class AndroidFrameMetricsSampler(
    private val benchmark: Benchmark,
) : FrameMetricsSampler {
    private var jankStats: JankStats? = null
    private var jankFrames = 0
    private var totalFrames = 0
    private var stallCount = 0
    private var stallTotalMs = 0.0
    private var slice = ""

    fun attachWindow(window: Window) {
        jankStats = JankStats.createAndTrack(window) { frameData ->
            totalFrames++
            if (frameData.isJank) jankFrames++
            val durationNs = frameData.frameDurationUiNanos
            if (durationNs > STALL_THRESHOLD_NS) {
                stallCount++
                stallTotalMs += durationNs / 1_000_000.0
            }
        }.also { it.isTrackingEnabled = false }
    }

    override fun start(slice: String) {
        this.slice = slice
        jankFrames = 0
        totalFrames = 0
        stallCount = 0
        stallTotalMs = 0.0
        jankStats?.isTrackingEnabled = true
    }

    override fun stop() {
        jankStats?.isTrackingEnabled = false
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
}

class AndroidCpuSampler(
    private val benchmark: Benchmark,
) : CpuSampler {
    private val handler = Handler(Looper.getMainLooper())
    private var slice = ""
    private var wallStartMs = 0L
    private var cpuStartMs = 0L
    private var peakPercent = 0.0
    private var lastWallMs = 0L
    private var lastCpuMs = 0L
    private var sampling = false

    private val sampleRunnable = object : Runnable {
        override fun run() {
            if (!sampling) return
            val wallNow = SystemClock.elapsedRealtime()
            val cpuNow = Process.getElapsedCpuTime()
            val wallDelta = (wallNow - lastWallMs).coerceAtLeast(1L)
            val cpuDelta = (cpuNow - lastCpuMs).coerceAtLeast(0L)
            val instant = (cpuDelta.toDouble() / wallDelta.toDouble()) * 100.0
            if (instant > peakPercent) peakPercent = instant
            lastWallMs = wallNow
            lastCpuMs = cpuNow
            handler.postDelayed(this, 50L)
        }
    }

    override fun start(slice: String) {
        this.slice = slice
        wallStartMs = SystemClock.elapsedRealtime()
        cpuStartMs = Process.getElapsedCpuTime()
        lastWallMs = wallStartMs
        lastCpuMs = cpuStartMs
        peakPercent = 0.0
        sampling = true
        handler.removeCallbacks(sampleRunnable)
        handler.post(sampleRunnable)
    }

    override fun stop() {
        if (!sampling) return
        sampling = false
        handler.removeCallbacks(sampleRunnable)
        val wallDelta = (SystemClock.elapsedRealtime() - wallStartMs).coerceAtLeast(1L)
        val cpuDelta = (Process.getElapsedCpuTime() - cpuStartMs).coerceAtLeast(0L)
        val avg = (cpuDelta.toDouble() / wallDelta.toDouble()) * 100.0
        if (avg > peakPercent) peakPercent = avg
        val tags = mapOf(MetricTag.SLICE to slice)
        benchmark.record(
            name = MetricNames.CPU_AVG,
            category = MetricCategory.CPU,
            value = avg,
            unit = Metric.UNIT_PERCENT,
            tags = tags,
        )
        benchmark.record(
            name = MetricNames.CPU_PEAK,
            category = MetricCategory.CPU,
            value = peakPercent,
            unit = Metric.UNIT_PERCENT,
            tags = tags,
        )
    }
}

class AndroidBenchmarkExporter(
    private val context: Context,
) : BenchmarkExporter {
    override suspend fun export(fileName: String, content: String): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "benchmark-exports").apply { mkdirs() }
            val file = File(dir, fileName)
            file.writeText(content)
            file.absolutePath
        }
}

fun androidColdStartElapsedMs(): Double =
    (SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()).toDouble()
