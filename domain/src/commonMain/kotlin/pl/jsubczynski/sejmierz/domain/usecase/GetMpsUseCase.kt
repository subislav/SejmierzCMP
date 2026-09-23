package pl.jsubczynski.sejmierz.domain.usecase

import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.MpRepository

class GetMpsUseCase(
    private val repository: MpRepository,
    private val benchmark: Benchmark,
) {
    suspend operator fun invoke(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Mp>> =
        benchmark.measured(
            category = MetricCategory.USECASE,
            name = "GetMpsUseCase",
            tags = mapOf(
                MetricTag.SOURCE to source.name,
                MetricTag.SLICE to "MPs",
            ),
        ) {
            repository.getMps(term, source, offset, limit)
        }
}
