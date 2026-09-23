package pl.jsubczynski.sejmierz.domain.repository

import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.Club
import pl.jsubczynski.sejmierz.domain.model.ClubDetail
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term

interface ClubRepository {

    suspend fun getClubs(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Club>>

    suspend fun getClub(
        term: Term,
        id: String,
        source: DataSource,
    ): DomainResult<ClubDetail>
}
