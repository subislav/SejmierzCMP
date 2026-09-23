package pl.jsubczynski.sejmierz.data.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.jsubczynski.sejmierz.data.benchmark.CompositeMetricSink
import pl.jsubczynski.sejmierz.data.benchmark.ConsoleMetricSink
import pl.jsubczynski.sejmierz.data.benchmark.EnrichingMetricSink
import pl.jsubczynski.sejmierz.data.benchmark.InMemoryMetricRepository
import pl.jsubczynski.sejmierz.data.local.ClubLocalDataSource
import pl.jsubczynski.sejmierz.data.local.MpLocalDataSource
import pl.jsubczynski.sejmierz.data.local.VotingLocalDataSource
import pl.jsubczynski.sejmierz.data.remote.HttpClientFactory
import pl.jsubczynski.sejmierz.data.remote.SejmApi
import pl.jsubczynski.sejmierz.data.repository.ClubRepositoryImpl
import pl.jsubczynski.sejmierz.data.repository.MpRepositoryImpl
import pl.jsubczynski.sejmierz.data.repository.VotingRepositoryImpl
import pl.jsubczynski.sejmierz.data.settings.InMemorySettingsRepository
import pl.jsubczynski.sejmierz.domain.benchmark.MetricRepository
import pl.jsubczynski.sejmierz.domain.benchmark.MetricSink
import pl.jsubczynski.sejmierz.domain.repository.ClubRepository
import pl.jsubczynski.sejmierz.domain.repository.MpRepository
import pl.jsubczynski.sejmierz.domain.repository.SettingsRepository
import pl.jsubczynski.sejmierz.domain.repository.VotingRepository

val dataModule: Module = module {





    single<MetricRepository> { InMemoryMetricRepository() }
    single { ConsoleMetricSink() }
    single<MetricSink> {
        EnrichingMetricSink(
            delegate = CompositeMetricSink(
                listOf(get<MetricRepository>(), get<ConsoleMetricSink>()),
            ),
            runContext = get(),
        )
    }


    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }


    single { HttpClientFactory.create(engine = get(), benchmark = get()) }
    single { SejmApi(client = get(), json = get(), benchmarkContext = get()) }


    single { MpLocalDataSource(database = get(), benchmark = get()) }
    single { VotingLocalDataSource(database = get(), benchmark = get()) }
    single { ClubLocalDataSource(database = get(), benchmark = get()) }


    single<MpRepository> { MpRepositoryImpl(api = get(), local = get(), cacheScope = get()) }
    single<VotingRepository> { VotingRepositoryImpl(api = get(), local = get(), cacheScope = get()) }
    single<ClubRepository> { ClubRepositoryImpl(api = get(), local = get(), cacheScope = get()) }
    single<SettingsRepository> { InMemorySettingsRepository() }
}
