package pl.jsubczynski.sejmierz.ios.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.jsubczynski.sejmierz.data.db.SejmierzDatabase
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkExporter
import pl.jsubczynski.sejmierz.domain.benchmark.CpuSampler
import pl.jsubczynski.sejmierz.domain.benchmark.DisplayTracker
import pl.jsubczynski.sejmierz.domain.benchmark.FrameMetricsSampler
import pl.jsubczynski.sejmierz.domain.benchmark.MemorySampler
import pl.jsubczynski.sejmierz.domain.benchmark.RunContextProvider
import pl.jsubczynski.sejmierz.ios.benchmark.IOSBenchmarkExporter
import pl.jsubczynski.sejmierz.ios.benchmark.IOSCpuSampler
import pl.jsubczynski.sejmierz.ios.benchmark.IOSDisplayTracker
import pl.jsubczynski.sejmierz.ios.benchmark.IOSFrameMetricsSampler
import pl.jsubczynski.sejmierz.ios.benchmark.IOSMemorySampler
import pl.jsubczynski.sejmierz.ios.benchmark.IOSRunContextProvider

val iosAppModule: Module = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<HttpClientEngine> { Darwin.create() }

    single<SqlDriver> {
        NativeSqliteDriver(
            schema = SejmierzDatabase.Schema,
            name = "sejmierz.db",
        )
    }
    single { SejmierzDatabase(get()) }

    single<RunContextProvider> { IOSRunContextProvider() }
    single<MemorySampler> { IOSMemorySampler() }
    single<DisplayTracker> { IOSDisplayTracker(benchmark = get()) }
    single<FrameMetricsSampler> { IOSFrameMetricsSampler(benchmark = get()) }
    single<CpuSampler> { IOSCpuSampler(benchmark = get()) }
    single<BenchmarkExporter> { IOSBenchmarkExporter() }
}
