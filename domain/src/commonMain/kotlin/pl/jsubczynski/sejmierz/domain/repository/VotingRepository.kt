package pl.jsubczynski.sejmierz.domain.repository

import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term
import pl.jsubczynski.sejmierz.domain.model.Voting
import pl.jsubczynski.sejmierz.domain.model.VotingDetail

interface VotingRepository {

    suspend fun getVotings(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Voting>>

    suspend fun getVoting(
        term: Term,
        sitting: Int,
        votingNumber: Int,
        source: DataSource,
    ): DomainResult<VotingDetail>
}
