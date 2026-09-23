package pl.jsubczynski.sejmierz.ui.core.benchmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import pl.jsubczynski.sejmierz.domain.benchmark.Metric
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricNames
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import kotlin.time.TimeSource

@Composable
fun ScreenDisplayTracking(
    slice: String,
    contentLoaded: Boolean,
) {
    val benchmark = LocalBenchmarkContext.current.benchmark
    val start = remember(slice) { TimeSource.Monotonic.markNow() }
    val reported = remember(slice) { DisplayReportState() }

    LaunchedEffect(slice) {
        withFrameNanos { }
        if (!reported.ttid) {
            benchmark.record(
                name = MetricNames.UI_TTID,
                category = MetricCategory.UI,
                value = start.elapsedNow().inWholeMicroseconds / 1000.0,
                unit = Metric.UNIT_MS,
                tags = mapOf(MetricTag.SLICE to slice),
            )
            reported.ttid = true
        }
    }

    LaunchedEffect(slice, contentLoaded) {
        if (contentLoaded && !reported.ttfd) {
            withFrameNanos { }
            benchmark.record(
                name = MetricNames.UI_TTFD,
                category = MetricCategory.UI,
                value = start.elapsedNow().inWholeMicroseconds / 1000.0,
                unit = Metric.UNIT_MS,
                tags = mapOf(MetricTag.SLICE to slice),
            )
            reported.ttfd = true
        }
    }
}

private class DisplayReportState(var ttid: Boolean = false, var ttfd: Boolean = false)
