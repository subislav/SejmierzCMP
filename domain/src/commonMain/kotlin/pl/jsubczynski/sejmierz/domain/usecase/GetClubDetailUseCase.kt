package pl.jsubczynski.sejmierz.domain.usecase

import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.ClubDetail
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.ClubRepository

class GetClubDetailUseCase(
    private val repository: ClubRepository,
    private val benchmark: Benchmark,
) {
    suspend operator fun invoke(
        term: Term,
        id: String,
        source: DataSource,
    ): DomainResult<ClubDetail> =
        benchmark.measured(
            category = MetricCategory.USECASE,
            name = "GetClubDetailUseCase",
            tags = mapOf(
                MetricTag.SOURCE to source.name,
                MetricTag.SLICE to "Clubs",
            ),
        ) {
            repository.getClub(term, id, source)
        }
}
