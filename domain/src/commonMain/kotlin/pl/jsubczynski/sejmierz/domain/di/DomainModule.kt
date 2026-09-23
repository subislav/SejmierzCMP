package pl.jsubczynski.sejmierz.domain.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkContext
import pl.jsubczynski.sejmierz.domain.benchmark.BenchmarkRunner
import pl.jsubczynski.sejmierz.domain.usecase.GetClubDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetClubsUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetMpDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetMpsUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingDetailUseCase
import pl.jsubczynski.sejmierz.domain.usecase.GetVotingsUseCase

val domainModule: Module = module {
    single { Benchmark(sink = get()) }
    single { BenchmarkRunner(benchmark = get()) }



    single {
        BenchmarkContext(
            benchmark = get(),
            memory = get(),
            frames = get(),
            display = get(),
            cpu = get(),
        )
    }

    factory { GetMpsUseCase(repository = get(), benchmark = get()) }
    factory { GetMpDetailUseCase(repository = get(), benchmark = get()) }

    factory { GetVotingsUseCase(repository = get(), benchmark = get()) }
    factory { GetVotingDetailUseCase(repository = get(), benchmark = get()) }

    factory { GetClubsUseCase(repository = get(), benchmark = get()) }
    factory { GetClubDetailUseCase(repository = get(), benchmark = get()) }
}
