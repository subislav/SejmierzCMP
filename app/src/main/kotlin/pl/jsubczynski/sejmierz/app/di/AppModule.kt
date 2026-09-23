package pl.jsubczynski.sejmierz.app.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.jsubczynski.sejmierz.app.benchmark.AndroidBenchmarkExporter
import pl.jsubczynski.sejmierz.app.benchmark.AndroidCpuSampler
import pl.jsubczynski.sejmierz.app.benchmark.AndroidDisplayTracker
import pl.jsubczynski.sejmierz.app.benchmark.AndroidFrameMetricsSampler
import pl.jsubczynski.sejmierz.app.benchmark.AndroidMemorySampler
import pl.jsubczynski.sejmierz.app.benchmark.AndroidRunContextProvider
import pl.jsubczynski.sejmierz.data.db.SejmierzDatabase
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkExporter
import pl.jsubczynski.sejmierz.domain.benchmark.CpuSampler
import pl.jsubczynski.sejmierz.domain.benchmark.DisplayTracker
import pl.jsubczynski.sejmierz.domain.benchmark.FrameMetricsSampler
import pl.jsubczynski.sejmierz.domain.benchmark.MemorySampler
import pl.jsubczynski.sejmierz.domain.benchmark.RunContextProvider

val appModule: Module = module {


    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }


    single<HttpClientEngine> { OkHttp.create() }


    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = SejmierzDatabase.Schema,
            context = androidContext(),
            name = "sejmierz.db",
        )
    }
    single { SejmierzDatabase(get()) }


    single<RunContextProvider> { AndroidRunContextProvider(context = androidContext()) }


    single<MemorySampler> { AndroidMemorySampler() }
    single<DisplayTracker> { AndroidDisplayTracker(benchmark = get()) }
    single<FrameMetricsSampler> { AndroidFrameMetricsSampler(benchmark = get()) }
    single<CpuSampler> { AndroidCpuSampler(benchmark = get()) }
    single<BenchmarkExporter> { AndroidBenchmarkExporter(context = androidContext()) }
}