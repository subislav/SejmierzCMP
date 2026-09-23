package pl.jsubczynski.sejmierz.ui.core.benchmark

import androidx.compose.runtime.staticCompositionLocalOf
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkContext

val LocalBenchmarkContext = staticCompositionLocalOf<BenchmarkContext> {
    error("BenchmarkContext not provided. Wrap content in CompositionLocalProvider.")
}
