package pl.jsubczynski.sejmierz.domain.usecase

import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.repository.ClubRepository

class GetClubsUseCase(
    private val repository: ClubRepository,
    private val benchmark: Benchmark,
) {
    suspend operator fun invoke(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Club>> =
        benchmark.measured(
            category = MetricCategory.USECASE,
            name = "GetClubsUseCase",
            tags = mapOf(
                MetricTag.SOURCE to source.name,
                MetricTag.SLICE to "Clubs",
            ),
        ) {
            repository.getClubs(term, source, offset, limit)
        }
}
