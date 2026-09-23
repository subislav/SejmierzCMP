package pl.jsubczynski.sejmierz.domain.usecase

import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.MpRepository

class GetMpDetailUseCase(
    private val repository: MpRepository,
    private val benchmark: Benchmark,
) {
    suspend operator fun invoke(
        term: Term,
        id: Int,
        source: DataSource,
    ): DomainResult<Mp> =
        benchmark.measured(
            category = MetricCategory.USECASE,
            name = "GetMpDetailUseCase",
            tags = mapOf(
                MetricTag.SOURCE to source.name,
                MetricTag.SLICE to "MPs",
            ),
        ) {
            repository.getMp(term, id, source)
        }
}
