package pl.jsubczynski.sejmierz.domain.usecase

import pl.jsubczynski.sejmierz.domain.benchmark.Benchmark
import pl.jsubczynski.sejmierz.domain.benchmark.MetricCategory
import pl.jsubczynski.sejmierz.domain.benchmark.MetricTag
import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.VotingDetail
import pl.jsubczynski.sejmierz.domain.repository.VotingRepository

class GetVotingDetailUseCase(
    private val repository: VotingRepository,
    private val benchmark: Benchmark,
) {
    suspend operator fun invoke(
        term: Term,
        sitting: Int,
        votingNumber: Int,
        source: DataSource,
    ): DomainResult<VotingDetail> =
        benchmark.measured(
            category = MetricCategory.USECASE,
            name = "GetVotingDetailUseCase",
            tags = mapOf(
                MetricTag.SOURCE to source.name,
                MetricTag.SLICE to "Votings",
            ),
        ) {
            repository.getVoting(term, sitting, votingNumber, source)
        }
}
