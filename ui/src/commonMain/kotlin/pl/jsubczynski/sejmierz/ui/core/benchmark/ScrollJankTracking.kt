package pl.jsubczynski.sejmierz.ui.core.benchmark

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import pl.jsubczynski.sejmierz.domain.benchmark.CpuSampler
import pl.jsubczynski.sejmierz.domain.benchmark.FrameMetricsSampler

@Composable
fun ScrollJankTracking(
    listState: LazyListState,
    sampler: FrameMetricsSampler,
    cpu: CpuSampler,
    slice: String,
) {
    val started = remember { BooleanHolder() }

    LaunchedEffect(listState, sampler, cpu, slice) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            when {
                scrolling && !started.value -> {
                    sampler.start(slice)
                    cpu.start(slice)
                    started.value = true
                }
                !scrolling && started.value -> {
                    sampler.stop()
                    cpu.stop()
                    started.value = false
                }
            }
        }
    }

    DisposableEffect(sampler, cpu) {
        onDispose {
            if (started.value) {
                sampler.stop()
                cpu.stop()
                started.value = false
            }
        }
    }
}

private class BooleanHolder(var value: Boolean = false)
