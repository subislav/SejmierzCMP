package pl.jsubczynski.sejmierz.domain.repository

import pl.jsubczynski.sejmierz.domain.error.DomainResult
import pl.jsubczynski.sejmierz.domain.model.DataSource
import pl.jsubczynski.sejmierz.domain.model.Mp
import pl.jsubczynski.sejmierz.domain.model.Page
import pl.jsubczynski.sejmierz.domain.model.Term

interface MpRepository {

    suspend fun getMps(
        term: Term,
        source: DataSource,
        offset: Int,
        limit: Int,
    ): DomainResult<Page<Mp>>

    suspend fun getMp(
        term: Term,
        id: Int,
        source: DataSource,
    ): DomainResult<Mp>
}
